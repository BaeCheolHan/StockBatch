package com.my.stock.config;

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

	protected final TransactionManager transactionManager;

	private final DataSource dataSource;

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
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException {

		SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
		scheduler.setDataSource(dataSource);
		scheduler.setTransactionManager((PlatformTransactionManager) transactionManager);
		scheduler.setQuartzProperties(quartzProperties());
		scheduler.setTriggers(QuartzJobUtil.getTriggers().toArray(new Trigger[QuartzJobUtil.getTriggers().size()]));
		scheduler.setJobDetails(QuartzJobUtil.getJobDetails().toArray(new JobDetail[QuartzJobUtil.getJobDetails().size()]));
		return scheduler;
	}

}