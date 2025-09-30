package com.my.stock.job.stock.list.update;
import com.my.stock.rdb.entity.Stocks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import com.my.stock.config.ScheduledBatch;

@Slf4j
@Configuration
@ScheduledBatch(job = "StocksListUpdateJob", cron = "0 0 1 1 * ?")
public class StocksListUpdateJobConfiguration {

    public StocksListUpdateJobConfiguration() {}

	@Bean
	public Job StocksListUpdateJob(JobRepository jobRepository, Step KosdaqStockListUpdateStep, Step KospiStockListUpdateStep, Step OverseaStockListUpdateStep) {
		return new JobBuilder("StocksListUpdateJob", jobRepository)
				.start(KosdaqStockListUpdateStep)
				.next(KospiStockListUpdateStep)
				.next(OverseaStockListUpdateStep)
				.build();
	}

	@Bean
	public Step KosdaqStockListUpdateStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager
			, StepExecutionListener krStockListStepListener, FlatFileItemReader<Stocks> kosdaqStocksItemReader, ItemWriter<Stocks> stocksItemWriter) {

		return new StepBuilder("KosdaqStockListUpdateStep", jobRepository)
				.<Stocks, Stocks>chunk(100, platformTransactionManager)
				.listener(krStockListStepListener)
				.reader(kosdaqStocksItemReader)
				.writer(stocksItemWriter)
				.faultTolerant()
				.skipLimit(0)
				.skip(FlatFileParseException.class)
				.build();
	}

	@Bean
	public Step KospiStockListUpdateStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, StepExecutionListener krStockListStepListener, FlatFileItemReader<Stocks> kospiStocksItemReader, ItemWriter<Stocks> stocksItemWriter) {
		return new StepBuilder("KospiStockListUpdateStep", jobRepository)
				.<Stocks, Stocks>chunk(100, platformTransactionManager)
				.listener(krStockListStepListener)
				.reader(kospiStocksItemReader)
				.writer(stocksItemWriter)
				.faultTolerant()
				.skipLimit(0)
				.skip(FlatFileParseException.class)
				.build();
	}

	@Bean
	public Step OverseaStockListUpdateStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, StepExecutionListener overseaStockListStepListener, MultiResourceItemReader<Stocks> overseaStockItemReader, ItemWriter<Stocks> stocksItemWriter) {
		return new StepBuilder("OverseaStockListUpdateStep", jobRepository)
				.<Stocks, Stocks>chunk(100, platformTransactionManager)
				.listener(overseaStockListStepListener)
				.reader(overseaStockItemReader)
				.writer(stocksItemWriter)
				.faultTolerant()
				.skipLimit(0)
				.skip(FlatFileParseException.class)
				.build();
	}

}
