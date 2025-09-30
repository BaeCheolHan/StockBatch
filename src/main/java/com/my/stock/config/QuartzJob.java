package com.my.stock.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.quartz.DisallowConcurrentExecution;

@Slf4j
@Component
@DisallowConcurrentExecution
public class QuartzJob extends QuartzJobBean implements InterruptableJob {
	@Override
	public void interrupt() { }

	@Override
	protected void executeInternal(JobExecutionContext context) {
		try {
			Job job = QuartzJobUtil.getJobLocator().getJob(context.getJobDetail().getJobDataMap().getString("jobName"));
			JobParameters params = new JobParametersBuilder()
					.addString("JobID", String.valueOf(System.currentTimeMillis()))
					.toJobParameters();
			QuartzJobUtil.getJobLauncher().run(job, params);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}