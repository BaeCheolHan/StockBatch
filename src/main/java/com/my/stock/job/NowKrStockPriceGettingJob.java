package com.my.stock.job;

import com.my.stock.api.KisApi;
import com.my.stock.base.BaseBatch;
import com.my.stock.dto.KrNowStockPriceWrapper;
import com.my.stock.dto.kis.request.KrStockPriceRequest;
import com.my.stock.rdb.repository.StockRepository;
import com.my.stock.redis.entity.KrNowStockPrice;
import com.my.stock.redis.repository.KrNowStockPriceRepository;
import com.my.stock.util.KisApiUtils;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Optional;


@Slf4j
@Configuration
public class NowKrStockPriceGettingJob extends BaseBatch {

	private final StockRepository stockRepository;

	private final KrNowStockPriceRepository krNowStockPriceRepository;

	private final KisApi kisApi;

	private final KisApiUtils kisApiUtils;


	public NowKrStockPriceGettingJob(StockRepository stockRepository, KisApiUtils kisApiUtils, KrNowStockPriceRepository krNowStockPriceRepository, KisApi kisApi) {
		super("NowKrStockPriceGettingJob", "0 0/10 8-17 * * ?", null);
		this.stockRepository = stockRepository;
		this.kisApiUtils = kisApiUtils;
		this.kisApi = kisApi;
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
				.<String, KrNowStockPriceWrapper>chunk(1, platformTransactionManager)
				.reader(stockSymbolReader)
				.processor(nowStockPriceProcessor)
				.writer(writer)
				.build();
	}

	private final ItemReader<String> stockSymbolReader = new ItemReader<>() {
		List<String> stockSymbols = null;

		@Override
		public String read() throws InterruptedException {
			if (stockSymbols == null) {
				stockSymbols = stockRepository.findSymbolByNationalGroupBySymbol("KR");
			}

			if (stockSymbols.isEmpty()) {
				stockSymbols = null;
				return null;
			}
			Thread.sleep(300);
			return stockSymbols.remove(0);
		}
	};

	private final ItemProcessor<String, KrNowStockPriceWrapper> nowStockPriceProcessor = new ItemProcessor<>() {
		@Override
		public KrNowStockPriceWrapper process(String symbol) throws Exception {
			HttpHeaders headers = kisApiUtils.getDefaultApiHeader();
			headers.add("tr_id", "FHKST01010100");

			KrStockPriceRequest request = KrStockPriceRequest.builder()
					.fid_cond_mrkt_div_code("J")
					.fid_input_iscd(symbol)
					.build();

			KrNowStockPriceWrapper response = kisApi.getKorStockPrice(headers, request);

			response.getOutput().setSymbol(symbol);
			return response;
		}
	};

	private final ItemWriter<KrNowStockPriceWrapper> writer = new ItemWriter<>() {
		@Override
		public void write(Chunk<? extends KrNowStockPriceWrapper> chunk) {
			chunk.forEach(item -> {
				Optional<KrNowStockPrice> entity = krNowStockPriceRepository.findById(item.getOutput().getSymbol());
				entity.ifPresent(krNowStockPriceRepository::delete);
				krNowStockPriceRepository.save(item.getOutput());
			});
		}
	};

}

