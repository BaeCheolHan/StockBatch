package com.mobfeed.batch.config;

import com.my.stock.config.QuartzJob;
import com.my.stock.config.QuartzJobUtil;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Configuration
@RequiredArgsConstructor
public class QuartzConfig implements InitializingBean {
	private final JobLauncher jobLauncher;

	private final JobLocator jobLocator;

	private final TransactionManager transactionManager;

	@Override
	public void afterPropertiesSet() {
		QuartzJobUtil.setJobLauncher(jobLauncher);
		QuartzJobUtil.setJobLocator(jobLocator);
	}

	@Bean
	public Properties quartzProperties() throws IOException {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
		propertiesFactoryBean.afterPropertiesSet();
		return propertiesFactoryBean.getObject();
	}

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
		return jobRegistryBeanPostProcessor;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) throws IOException {
		List<Trigger> listTrigger = new ArrayList<>();
		List<JobDetail> listJobDetail = new ArrayList<>();
		for (Map<String, Object> jobInfo : QuartzJobUtil.getListBatchJob()) {
			listJobDetail.add(createJobDetail(createJobDataMap((String) jobInfo.get("jobName"))));
			if (jobInfo.containsKey("cronExpr")) {
				listTrigger.add(createJobTrigger(listJobDetail.get(listJobDetail.size() - 1), (String) jobInfo.get("cronExpr")));
			} else {
				listTrigger.add(createJobTrigger(listJobDetail.get(listJobDetail.size() - 1), Integer.parseInt(jobInfo.get("timeInterval").toString())));
			}
		}

		SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
		scheduler.setDataSource(dataSource);
		scheduler.setTransactionManager((PlatformTransactionManager) transactionManager);
		scheduler.setQuartzProperties(quartzProperties());
		scheduler.setTriggers(listTrigger.toArray(new Trigger[listTrigger.size()]));
		scheduler.setJobDetails(listJobDetail.toArray(new JobDetail[listJobDetail.size()]));
		return scheduler;
	}

	private JobDataMap createJobDataMap(String jobName) {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("jobName", jobName);
		return jobDataMap;
	}

	private JobDetail createJobDetail(JobDataMap jobDataMap) {
		return JobBuilder
				.newJob(QuartzJob.class)
				.withIdentity(jobDataMap.getString("jobName").concat("Detail"))
				.setJobData(jobDataMap)
				.storeDurably()
				.build();
	}

	private Trigger createJobTrigger(JobDetail jobDetail, int value) {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(value).repeatForever();
		return TriggerBuilder
				.newTrigger()
				.forJob(jobDetail)
				.withIdentity(jobDetail.getJobDataMap().getString("jobName").concat("Trigger"))
				.withSchedule(scheduleBuilder)
				.build();
	}

	private Trigger createJobTrigger(JobDetail jobDetail, String expr) {
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(expr);
		return TriggerBuilder
				.newTrigger()
				.forJob(jobDetail)
				.withIdentity(jobDetail.getJobDataMap().getString("jobName").concat("Trigger"))
				.withSchedule(scheduleBuilder)
				.build();
	}
}