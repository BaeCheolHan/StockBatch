package com.my.stock.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.stock.base.BaseBatch;
import com.my.stock.dto.KrNowStockPriceWrapper;
import com.my.stock.rdb.repository.StockRepository;
import com.my.stock.redis.entity.KrNowStockPrice;
import com.my.stock.redis.entity.OverSeaNowStockPrice;
import com.my.stock.redis.repository.KrNowStockPriceRepository;
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

	private final KrNowStockPriceRepository krNowStockPriceRepository;

	private final KisTokenProvider kisTokenProvider;


	public NowOverSeaStockPriceGettingJob(StockRepository stockRepository, KisTokenProvider kisTokenProvider, KrNowStockPriceRepository krNowStockPriceRepository) {
		super("NowKrStockPriceGettingJob", "0 0/20 8-17 * * ?", null);
		this.stockRepository = stockRepository;
		this.kisTokenProvider = kisTokenProvider;
		this.krNowStockPriceRepository = krNowStockPriceRepository;
	}

	@Bean
	public Job NowKrStockPriceGettingJob(JobRepository jobRepository, Step nowKrStockPriceGettingStep) {

		return new JobBuilder("NowKrStockPriceGettingJob", jobRepository)
				.start(nowKrStockPriceGettingStep)
				.build();
	}

	@Bean
	public Step nowKrStockPriceGettingStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("nowKrStockPriceGettingStep", jobRepository)
				.<String, OverSeaNowStockPrice>chunk(1, platformTransactionManager)
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
				stockSymbols = stockRepository.findSymbolGroupBySymbol("KR");
			}

			if (stockSymbols.isEmpty()) {
				return null;
			}
			return stockSymbols.remove(0);
		}
	};

	private final ItemProcessor<String, OverSeaNowStockPrice> nowStockPriceProcessor = new ItemProcessor<>() {
		@Override
		public OverSeaNowStockPrice process(String item) throws Exception {
			RestKisToken kisToken = kisTokenProvider.getRestToken();
			HttpHeaders headers = new HttpHeaders();
			headers.add("authorization", kisToken.getToken_type() + " " + kisToken.getAccess_token());
			headers.add("content-type", 	"application/json; charset=utf-8");
			headers.add("appkey", appKey);
			headers.add("appsecret", appSecret);
			headers.add("tr_id", "HHDFS76200200");
			headers.add("custtype", "P");

			HashMap<String, Object> param = new HashMap<>();
			param.put("FID_COND_MRKT_DIV_CODE", "J");
			param.put("FID_INPUT_ISCD", item);
			OverSeaNowStockPrice response = new ObjectMapper().readValue(ApiCaller.getInstance()
					.get("https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-price", headers, param)
					, OverSeaNowStockPrice.class);
			return response;
		}
	};

	private final ItemWriter<OverSeaNowStockPrice> writer = new ItemWriter<>() {
		@Override
		public void write(Chunk<? extends OverSeaNowStockPrice> chunk) {
			chunk.forEach(item -> {
				Optional<KrNowStockPrice> entity = krNowStockPriceRepository.findById(item.getStck_shrn_iscd());
				entity.ifPresent(krNowStockPriceRepository::delete);
				krNowStockPriceRepository.save(item);
			});
		}
	};

}

