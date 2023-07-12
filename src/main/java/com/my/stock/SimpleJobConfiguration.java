package com.my.stock;


import com.my.stock.base.BaseBatch;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
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
		super("simpleJob", "0 0/20 * * * ?", null);
	}



	@Bean
	public Job simpleJob(JobRepository jobRepository, Step simpleStep) {
		return new JobBuilder("simpleJob", jobRepository)
				.listener(jobListener)
				.start(simpleStep)
				.build();
	}

	private final JobExecutionListener jobListener = new JobExecutionListener() {

		@Override
		public void beforeJob(JobExecution jobExecution) {
			System.out.println(" + " + jobExecution.getJobParameters().getString("test"));
		}

		@Override
		public void afterJob(JobExecution jobExecution) {
		}


	};

	@Bean
	public Step simpleStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("simpleStep", jobRepository)
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> This is Step");
					return RepeatStatus.FINISHED;
				}, platformTransactionManager).build();
	}

}