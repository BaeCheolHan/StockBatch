package com.my.stock.job;

import com.my.stock.base.BaseBatch;
import com.my.stock.rdb.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;


@Slf4j
@Configuration
public class NowStockPriceGettingJob extends BaseBatch {

	private final StockRepository stockRepository;

	public NowStockPriceGettingJob(StockRepository stockRepository) {
		super("NowStockPriceGettingJob", "0 0/20 * * * ?", null);
		this.stockRepository = stockRepository;
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

	private final ItemProcessor<String, String> nowStockPriceProcessor = item -> item + " test";

	private final ItemWriter<String> writer = item -> {
		System.out.println(item);
	};

}

