package com.my.stock;


import com.my.stock.base.BaseBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class SimpleJobConfiguration extends BaseBatch {
	public SimpleJobConfiguration() {
		super("simpleJob", "0/1 * * * * ?", null);
	}

	@Bean
	public Job simpleJob1(JobRepository jobRepository, Step simpleStep1) {
		return new JobBuilder("simpleJob", jobRepository)
				.start(simpleStep1)
				.build();
	}

	@Bean
	public Step simpleStep1(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("simpleStep1", jobRepository)
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> This is Step1");
					return RepeatStatus.FINISHED;
				}, platformTransactionManager).build();
	}

}