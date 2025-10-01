package com.my.stock.job;

import com.my.stock.api.KisApi;
import com.my.stock.config.QuartzJobUtil;
import com.my.stock.dto.OverSeaNowStockPriceWrapper;
import com.my.stock.dto.SymbolAndCodeInterface;
import com.my.stock.dto.kis.request.OverSeaStockPriceRequest;
import com.my.stock.rdb.repository.StockRepository;
import com.my.stock.redis.entity.OverSeaNowStockPrice;
import com.my.stock.redis.repository.OverSeaNowStockPriceRepository;
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
import com.my.stock.config.ScheduledBatch;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Slf4j
@Configuration
@ScheduledBatch(job = "ToNightOverSeaStockPriceGettingJob", cron = "0 0/10 20-23 * * ?")
public class NowOverSeaStockPriceGettingJobConfiguration {

	private final StockRepository stockRepository;

	private final OverSeaNowStockPriceRepository overSeaNowStockPriceRepository;

	private final KisApi kisApi;

	private final KisApiUtils kisApiUtils;

    public NowOverSeaStockPriceGettingJobConfiguration(StockRepository stockRepository, KisApiUtils kisApiUtils
            , OverSeaNowStockPriceRepository overSeaNowStockPriceRepository, KisApi kisApi) {
        QuartzJobUtil.getJobDetails().add(QuartzJobUtil.QuartzJobUtilHelper.buildJobDetail("OverNightOverSeaStockPriceGettingJob"));
        QuartzJobUtil.getTriggers().add(QuartzJobUtil.QuartzJobUtilHelper.buildCronTrigger("OverNightOverSeaStockPriceGettingJob", "0 0/10 0-9 * * ?"));
		this.stockRepository = stockRepository;
		this.kisApiUtils = kisApiUtils;
		this.overSeaNowStockPriceRepository = overSeaNowStockPriceRepository;
		this.kisApi = kisApi;
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

			HttpHeaders headers = kisApiUtils.getDefaultApiHeader();
			headers.add("tr_id", "HHDFS76200200");
			headers.add("custtype", "P");
			OverSeaNowStockPriceWrapper response = kisApi.getOverSeaStockPrice(headers, OverSeaStockPriceRequest.builder()
					.AUTH("")
					.EXCD(item.getCode())
					.SYMB(item.getSymbol())
					.build());

			response.getOutput().setSymbol(item.getSymbol());
			return response;
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

