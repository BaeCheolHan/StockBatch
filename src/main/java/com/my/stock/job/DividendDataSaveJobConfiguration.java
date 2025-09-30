package com.my.stock.job;

import com.my.stock.base.BaseBatch;
import com.my.stock.dto.StockDividendHistory;
import com.my.stock.dto.kis.response.KrStockVolumeRankOutput;
import com.my.stock.rdb.entity.Stocks;
import com.my.stock.rdb.repository.StockRepository;
import com.my.stock.rdb.repository.StocksRepository;
import com.my.stock.redis.entity.DividendInfo;
import com.my.stock.redis.entity.KrStockVolumeRank;
import com.my.stock.redis.repository.DividendInfoRepository;
import com.my.stock.redis.repository.KrStockVolumeRankRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import com.my.stock.api.YfinApi;
import com.my.stock.service.YfinResilientClient;
import com.my.stock.dto.yfin.DividendsResponse;
import com.my.stock.dto.yfin.QuoteDto;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class DividendDataSaveJobConfiguration extends BaseBatch {

	private final StocksRepository stocksRepository;

	private final StockRepository stockRepository;

	private final DividendInfoRepository dividendInfoRepository;

	private final KrStockVolumeRankRepository krStockVolumeRankRepository;

    private final YfinResilientClient yfinClient;

    public DividendDataSaveJobConfiguration(DividendInfoRepository dividendInfoRepository, StocksRepository stocksRepository, StockRepository stockRepository, KrStockVolumeRankRepository krStockVolumeRankRepository, YfinApi yfinApi, YfinResilientClient yfinClient) {
		super("DividendDataSaveJob", "0 0 * * * ?", null);

		this.dividendInfoRepository = dividendInfoRepository;
		this.stocksRepository = stocksRepository;
		this.stockRepository = stockRepository;
        this.krStockVolumeRankRepository = krStockVolumeRankRepository;
        this.yfinClient = yfinClient;
	}

	@Bean
	public Job DividendDataSaveJob(JobRepository jobRepository, Step dividendDataSaveJobStep) {
		return new JobBuilder("DividendDataSaveJob", jobRepository)
				.start(dividendDataSaveJobStep)
				.build();
	}

	@Bean
	public Step dividendDataSaveJobStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
				ItemReader<Stocks> dividendStockReader,
				ItemProcessor<Stocks, DividendInfo> dividendDataProcessor,
				ItemWriter<DividendInfo> dividendWriter) {
		return new StepBuilder("dividendDataSaveJobStep", jobRepository)
				.<Stocks, DividendInfo>chunk(10, platformTransactionManager)
				.reader(dividendStockReader)
				.processor(dividendDataProcessor)
				.writer(dividendWriter)
				.faultTolerant()
                .retryLimit(3)
				.retry(Exception.class)
				.skipLimit(50)
				.skip(Exception.class)
				.build();
	}

	@Bean
	@StepScope
	public ItemReader<Stocks> dividendStockReader(@Value("#{jobParameters['symbol']}") String targetSymbol) {
		return new ItemReader<>() {
			List<Stocks> stockSymbols = null;

			@Override
			public Stocks read() throws Exception {
				if (stockSymbols == null) {
					if (targetSymbol != null) {
						stockSymbols = new ArrayList<>();
						stockSymbols.add(stocksRepository.findBySymbol(targetSymbol).orElseThrow(Exception::new));
					} else {
						List<DividendInfo> l = (List<DividendInfo>) dividendInfoRepository.findAll();
						List<String> symbols = stockRepository.findSymbolAllLeftJoinDividendGropBySymbol();

						Optional<KrStockVolumeRank> krStockVolumeRanks = krStockVolumeRankRepository.findById("0000");

						List<String> krStockVolumeRankSymbols = krStockVolumeRanks.stream()
								.map(it -> it.getData().getOutput().stream().map(KrStockVolumeRankOutput::getMksc_shrn_iscd).collect(Collectors.toList()))
								.flatMap(List::stream)
								.toList();

						List<String> joinedSymbols = Stream.of(symbols.stream(), l.stream().map(DividendInfo::getSymbol), krStockVolumeRankSymbols.stream())
								.flatMap(stringStream -> stringStream)
								.distinct()
								.toList();

						stockSymbols = stocksRepository.findAllBySymbolIn(joinedSymbols);
					}
				}

				if (stockSymbols.isEmpty()) {
					stockSymbols = null;
					return null;
				}

				return stockSymbols.remove(0);
			}
		};
	}

	@Bean
	@StepScope
    public ItemProcessor<Stocks, DividendInfo> dividendDataProcessor() { return stocks -> {
		DividendInfo dividendInfo = new DividendInfo();
		try {
			String symbol = stocks.getSymbol();

			// yfin 서버에서 심볼 정규화를 처리하므로 원본 심볼을 그대로 전달

            QuoteDto quote = yfinClient.getQuote(symbol, null);
            DividendsResponse dividends = yfinClient.getDividends(symbol, "5y", null);

			dividendInfo.setSymbol(stocks.getSymbol());
			if (quote != null) {
				Double rate = (quote.getForwardDividendRate() != null) ? quote.getForwardDividendRate() : quote.getTrailingAnnualDividendRate();
				if (rate != null) dividendInfo.setAnnualDividend(BigDecimal.valueOf(rate));

				Double yieldPct = (quote.getForwardDividendYieldPct() != null)
						? quote.getForwardDividendYieldPct()
						: (quote.getTrailingAnnualDividendYield() != null ? quote.getTrailingAnnualDividendYield() * 100.0 : null);
				if (yieldPct != null) dividendInfo.setDividendRate(BigDecimal.valueOf(yieldPct));
			}

			if (dividends != null && dividends.getRows() != null && !dividends.getRows().isEmpty()) {
				dividendInfo.setDividendHistories(dividends.getRows().stream()
						.map(r -> StockDividendHistory.builder()
								.symbol(stocks.getSymbol())
								.dividend(r.getAmount() == null ? null : BigDecimal.valueOf(r.getAmount()))
								.date(r.getDate())
								.build())
						.toList());
			}

		} catch (Exception ignore) {
			ignore.printStackTrace();
			dividendInfo = new DividendInfo();
		}

		return dividendInfo;
	}; }

	@Bean
	@StepScope
	public ItemWriter<DividendInfo> dividendWriter() {
		return new ItemWriter<>() {
			@Override
			public void write(Chunk<? extends DividendInfo> chunk) {
				chunk.forEach(dividendInfoRepository::save);
			}
		};
	}
}
