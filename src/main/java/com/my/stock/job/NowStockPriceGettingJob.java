package com.my.stock.job;

import com.my.stock.base.BaseBatch;
import com.my.stock.rdb.repository.StockRepository;
import com.my.stock.util.ApiCaller;
import com.my.stock.util.KisTokenProvider;
import com.my.stock.util.RestKisToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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


@Slf4j
@Configuration
public class NowStockPriceGettingJob extends BaseBatch {

	@Value("${api.kis.appKey}")
	private String appKey;

	@Value("${api.kis.app-secret}")
	private String appSecret;

	private final StockRepository stockRepository;

	private final KisTokenProvider kisTokenProvider;


	public NowStockPriceGettingJob(StockRepository stockRepository, KisTokenProvider kisTokenProvider) {
		super("NowStockPriceGettingJob", "0 0/20 * * * ?", null);
		this.stockRepository = stockRepository;
		this.kisTokenProvider = kisTokenProvider;
	}

	@Bean
	public Job NowStockPriceGettingJob(JobRepository jobRepository, Step nowStockPriceGettingStep) {

		return new JobBuilder("NowStockPriceGettingJob", jobRepository)
				.start(nowStockPriceGettingStep)
				.build();
	}

	@Bean
	public Step nowStockPriceGettingStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("nowStockPriceGettingStep", jobRepository)
				.<String, String>chunk(1, platformTransactionManager)
				.reader(stockSymbolReader)
				.processor(nowStockPriceProcessor)
				.writer(writer)
				.build();
	}

	private final ItemReader<String> stockSymbolReader = new ItemReader<>() {
		List<String> stockSymbols = null;

		@Override
		public String read() {
			if (stockSymbols == null) {
				stockSymbols = stockRepository.findSymbolGroupBySymbol();
			}

			if (stockSymbols.isEmpty()) {
				return null;
			}
			return stockSymbols.remove(0);
		}
	};

	private final ItemProcessor<String, String> nowStockPriceProcessor = new ItemProcessor<>() {
		@Override
		public String process(String item) throws Exception {
			RestKisToken kisToken = kisTokenProvider.getRestToken();
			HttpHeaders headers = new HttpHeaders();
			headers.add("authorization", "Bearer " + kisToken.getAccess_token());
			headers.add("appkey", appKey);
			headers.add("appsecret", appSecret);
			headers.add("tr_id", "FHKST01010100 ");

			HashMap<String, Object> param = new HashMap<>();
			param.put("FID_COND_MRKT_DIV_CODE", "J");
			param.put("FID_INPUT_ISCD", item);
			String response = ApiCaller.getInstance().get("https://openapi.koreainvestment.com:9443", headers, param);
			return response;
		}
	};

	private final ItemWriter<String> writer = item -> {
		System.out.println(item);
	};

}

