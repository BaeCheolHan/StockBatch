package com.my.stock.job;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.stock.base.BaseBatch;
import com.my.stock.config.QuartzJobUtil;
import com.my.stock.dto.OverSeaNowStockPriceWrapper;
import com.my.stock.dto.SymbolAndCodeInterface;
import com.my.stock.rdb.repository.StockRepository;
import com.my.stock.redis.entity.OverSeaNowStockPrice;
import com.my.stock.redis.repository.OverSeaNowStockPriceRepository;
import com.my.stock.util.ApiCaller;
import com.my.stock.util.KisTokenProvider;
import com.my.stock.util.RestKisToken;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Slf4j
@Configuration
public class NowOverSeaStockPriceGettingJob extends BaseBatch {

	@Value("${api.kis.appKey}")
	private String appKey;

	@Value("${api.kis.app-secret}")
	private String appSecret;

	private final StockRepository stockRepository;

	private final OverSeaNowStockPriceRepository overSeaNowStockPriceRepository;

	private final KisTokenProvider kisTokenProvider;


	public NowOverSeaStockPriceGettingJob(StockRepository stockRepository, KisTokenProvider kisTokenProvider, OverSeaNowStockPriceRepository overSeaNowStockPriceRepository) {
		super("ToNightOverSeaStockPriceGettingJob", "0 0/10 20-23 * * ?", null);

		QuartzJobUtil.getJobDetails().add(buildJobDetail("OverNightOverSeaStockPriceGettingJob", new HashMap<>()));
		QuartzJobUtil.getTriggers().add(buildJobTrigger("OverNightOverSeaStockPriceGettingJob", "0 0/10 0-9 * * ?", new HashMap<>()));
		this.stockRepository = stockRepository;
		this.kisTokenProvider = kisTokenProvider;
		this.overSeaNowStockPriceRepository = overSeaNowStockPriceRepository;
	}

	// job 1
	@Bean
	public Job ToNightOverSeaStockPriceGettingJob(JobRepository jobRepository, Step nowOverSeaStockPriceGettingStep) {
		return new JobBuilder("ToNightOverSeaStockPriceGettingJob", jobRepository)
				.start(nowOverSeaStockPriceGettingStep)
				.build();
	}

	// job 2
	@Bean
	public Job OverNightOverSeaStockPriceGettingJob(JobRepository jobRepository, Step nowOverSeaStockPriceGettingStep) {
		return new JobBuilder("OverNightOverSeaStockPriceGettingJob", jobRepository)
				.start(nowOverSeaStockPriceGettingStep)
				.build();
	}


	// step
	@Bean
	public Step nowOverSeaStockPriceGettingStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("nowOverSeaStockPriceGettingStep", jobRepository)
				.<SymbolAndCodeInterface, OverSeaNowStockPriceWrapper>chunk(1, platformTransactionManager)
				.reader(stockSymbolReader)
				.processor(nowStockPriceProcessor)
				.writer(writer)
				.build();
	}

	private final ItemReader<SymbolAndCodeInterface> stockSymbolReader = new ItemReader<>() {
		List<SymbolAndCodeInterface> stockSymbols = null;

		@Override
		public SymbolAndCodeInterface read() throws InterruptedException {
			if (stockSymbols == null) {
				stockSymbols = stockRepository.findSymbolAndCodeNotNationalGroupBySymbol("KR");
			}

			if (stockSymbols.isEmpty()) {
				stockSymbols = null;
				return null;
			}
			Thread.sleep(300);
			return stockSymbols.remove(0);
		}
	};

	private final ItemProcessor<SymbolAndCodeInterface, OverSeaNowStockPriceWrapper> nowStockPriceProcessor = new ItemProcessor<>() {
		@Override
		public OverSeaNowStockPriceWrapper process(SymbolAndCodeInterface item) throws Exception {
			RestKisToken kisToken = kisTokenProvider.getRestToken();
			HttpHeaders headers = new HttpHeaders();
			headers.add("authorization", kisToken.getToken_type() + " " + kisToken.getAccess_token());
			headers.add("content-type", "application/json; charset=utf-8");
			headers.add("appkey", appKey);
			headers.add("appsecret", appSecret);
			headers.add("tr_id", "HHDFS76200200");
			headers.add("custtype", "P");

			HashMap<String, Object> param = new HashMap<>();
			param.put("AUTH", "");
			param.put("EXCD", item.getCode());
			param.put("SYMB", item.getSymbol());
			OverSeaNowStockPriceWrapper wrapper = new ObjectMapper()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.readValue(ApiCaller.getInstance().get("https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/price-detail", headers, param)
							, OverSeaNowStockPriceWrapper.class);

			wrapper.getOutput().setSymbol(item.getSymbol());
			return wrapper;
		}
	};

	private final ItemWriter<OverSeaNowStockPriceWrapper> writer = new ItemWriter<>() {
		@Override
		public void write(Chunk<? extends OverSeaNowStockPriceWrapper> chunk) {
			chunk.forEach(item -> {
				Optional<OverSeaNowStockPrice> entity = overSeaNowStockPriceRepository.findById(item.getOutput().getSymbol());
				entity.ifPresent(overSeaNowStockPriceRepository::delete);
				overSeaNowStockPriceRepository.save(item.getOutput());
			});
		}
	};

}

