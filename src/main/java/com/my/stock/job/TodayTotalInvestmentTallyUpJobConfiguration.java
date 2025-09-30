package com.my.stock.job;
import com.my.stock.dto.KrNowStockPriceWrapper;
import com.my.stock.dto.OverSeaNowStockPriceWrapper;
import com.my.stock.dto.kis.request.KrStockPriceRequest;
import com.my.stock.dto.kis.request.OverSeaStockPriceRequest;
import com.my.stock.rdb.entity.*;
import com.my.stock.rdb.repository.BankAccountRepository;
import com.my.stock.rdb.repository.DailyTotalInvestmentAmountRepository;
import com.my.stock.rdb.repository.ExchangeRateRepository;
import com.my.stock.rdb.repository.StocksRepository;
import com.my.stock.redis.entity.KrNowStockPrice;
import com.my.stock.redis.entity.OverSeaNowStockPrice;
import com.my.stock.redis.repository.KrNowStockPriceRepository;
import com.my.stock.redis.repository.OverSeaNowStockPriceRepository;
import com.my.stock.util.KisApiUtils;
import com.my.stock.service.KisResilientClient;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.batch.core.configuration.annotation.StepScope;
import com.my.stock.config.ScheduledBatch;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@ScheduledBatch(job = "TodayTotalInvestmentTallyUpJob", cron = "0 1 0 * * ?")
@RequiredArgsConstructor
public class TodayTotalInvestmentTallyUpJobConfiguration {


	private final StocksRepository stocksRepository;

	private final ExchangeRateRepository exchangeRateRepository;

	private final KrNowStockPriceRepository krNowStockPriceRepository;

	private final OverSeaNowStockPriceRepository overSeaNowStockPriceRepository;

	private final EntityManagerFactory entityManagerFactory;

	private final BankAccountRepository bankAccountRepository;

	private final DailyTotalInvestmentAmountRepository dailyTotalInvestmentAmountRepository;

	private final KisApiUtils kisApiUtils;

    private final KisResilientClient kisClient;


    

	@Bean
	public Job TodayTotalInvestmentTallyUpJob(JobRepository jobRepository, Step TodayTotalInvestmentTallyUpStep) {
		return new JobBuilder("TodayTotalInvestmentTallyUpJob", jobRepository)
				.start(TodayTotalInvestmentTallyUpStep)
				.build();
	}

    @Bean
    @Transactional
    public Step TodayTotalInvestmentTallyUpStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("TodayTotalInvestmentTallyUpStep", jobRepository)
				.<Member, DailyTotalInvestmentAmount>chunk(10, platformTransactionManager)
				.reader(memberReader())
				.processor(todayTotalInvestmentTallyUpProcessor())
				.writer(todayTotalInvestmentTallyUpWriter())
                .faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(50).skip(Exception.class)
				.build();
	}

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> memberReader() {
		return new JpaPagingItemReaderBuilder<Member>()
				.name("memberReader")
				.entityManagerFactory(entityManagerFactory)
				.pageSize(1)
				.queryString("SELECT m FROM Member m")
				.build();
	}

    @Bean
    @StepScope
    public ItemProcessor<Member, DailyTotalInvestmentAmount> todayTotalInvestmentTallyUpProcessor() {

		return member -> {

			List<BankAccount> accounts = bankAccountRepository.findAllByMemberId(member.getId());
			BigDecimal totalInvestmentAmount = BigDecimal.ZERO;
			BigDecimal totalEvaluationAmount = BigDecimal.ZERO;
			ExchangeRate exchangeRateEntity = exchangeRateRepository.findFirstByOrderByIdDesc();

			for (BankAccount account : accounts) {
				totalInvestmentAmount = totalInvestmentAmount.add(account.getStocks().stream().map(stock -> {
					Stocks stocks = stocksRepository.findBySymbol(stock.getSymbol()).orElseThrow(() -> new RuntimeException("NOT FOUND SYMBOL"));
					if (stocks.getNational().equals("KR")) {
						return stock.getQuantity().multiply(stock.getPrice());
					} else {
						return stock.getQuantity().multiply(stock.getPrice()).multiply(exchangeRateEntity.getBasePrice());
					}
				}).reduce(BigDecimal.ZERO, BigDecimal::add));

				totalEvaluationAmount = totalEvaluationAmount.add(account.getStocks().stream().map(stock -> {

					Stocks stocks = stocksRepository.findBySymbol(stock.getSymbol()).orElseThrow(() -> new RuntimeException("NOT FOUND SYMBOL"));

					if (stocks.getNational().equals("KR")) {
						KrNowStockPrice entity = krNowStockPriceRepository.findById(stock.getSymbol())
								.orElseGet(() -> {
									try {
										this.saveNowPrice(stock.getSymbol());
									} catch (Exception e) {
										e.printStackTrace();
										throw new RuntimeException(e);
									}
									return krNowStockPriceRepository.findById(stock.getSymbol()).orElseThrow(() -> new RuntimeException("NOT FOUND SYMBOL"));
								});
						return stock.getQuantity().multiply(entity.getStck_prpr());
					} else {
						OverSeaNowStockPrice entity = overSeaNowStockPriceRepository.findById("D".concat(stocks.getCode()).concat(stock.getSymbol()))
								.orElseGet(() -> {
									try {
										this.saveNowPrice(stock.getSymbol());
									} catch (Exception e) {
										e.printStackTrace();
										throw new RuntimeException(e);
									}
									return overSeaNowStockPriceRepository.findById(stock.getSymbol()).orElseThrow(() -> new RuntimeException("NOT FOUND SYMBOL"));
								});

						return stock.getQuantity().multiply(entity.getLast()).multiply(exchangeRateEntity.getBasePrice());
					}
				}).reduce(BigDecimal.ZERO, BigDecimal::add));
			}

			DailyTotalInvestmentAmount investmentAmount = new DailyTotalInvestmentAmount();
			investmentAmount.setDate(LocalDate.now());
			investmentAmount.setTotalInvestmentAmount(totalInvestmentAmount);
			investmentAmount.setEvaluationAmount(totalEvaluationAmount);
			investmentAmount.setMember(member);
			return investmentAmount;
		};
	}

    @Bean
    @StepScope
    public ItemWriter<DailyTotalInvestmentAmount> todayTotalInvestmentTallyUpWriter() {
		return list -> list.forEach(d -> {
			if (!d.getTotalInvestmentAmount().equals(BigDecimal.ZERO))
				dailyTotalInvestmentAmountRepository.upsert(d);
		});
	}


	private void saveNowPrice(String symbol) throws Exception {
		Stocks stocks = stocksRepository.findBySymbol(symbol).orElseThrow(() -> new RuntimeException("NOT FOUND SYMBOL"));
		HttpHeaders headers = kisApiUtils.getDefaultApiHeader();
		if (!stocks.getNational().equals("KR")) {
			headers.add("tr_id", "HHDFS76200200");
			headers.add("custtype", "P");

            OverSeaNowStockPriceWrapper response = kisClient.getOverSeaStockPrice(headers, OverSeaStockPriceRequest.builder()
					.AUTH("")
					.EXCD(stocks.getCode())
					.SYMB(stocks.getSymbol())
					.build());

			response.getOutput().setSymbol(symbol);

			Optional<OverSeaNowStockPrice> entity = overSeaNowStockPriceRepository.findById(response.getOutput().getSymbol());
			entity.ifPresent(overSeaNowStockPriceRepository::delete);
			overSeaNowStockPriceRepository.save(response.getOutput());
		} else {
			headers.add("tr_id", "FHKST01010100");

			KrStockPriceRequest request = KrStockPriceRequest.builder()
					.fid_cond_mrkt_div_code("J")
					.fid_input_iscd(symbol)
					.build();

            KrNowStockPriceWrapper response = kisClient.getKorStockPrice(headers, request);

			response.getOutput().setSymbol(symbol);
			Optional<KrNowStockPrice> entity = krNowStockPriceRepository.findById(response.getOutput().getSymbol());
			entity.ifPresent(krNowStockPriceRepository::delete);
			krNowStockPriceRepository.save(response.getOutput());
		}
	}

}
