package com.my.stock.base;

import com.my.stock.config.QuartzJob;
import com.my.stock.config.QuartzJobUtil;
import org.quartz.*;

import java.util.HashMap;
import java.util.Map;

import static org.quartz.JobBuilder.newJob;

public abstract class BaseBatch {
	public BaseBatch(String jobName, String cronExp, HashMap<String, Object> param) {
		if (param == null) param = new HashMap<>();
		QuartzJobUtil.getJobDetails().add(buildJobDetail(jobName, param));
		QuartzJobUtil.getTriggers().add(buildJobTrigger(jobName, cronExp, param));

	}

	public BaseBatch(String jobName, int timeInterval, HashMap<String, Object> param) {
		QuartzJobUtil.getJobDetails().add(buildJobDetail(jobName, param));
		QuartzJobUtil.getTriggers().add(buildJobTrigger(jobName, timeInterval, param));
	}

	public Trigger buildJobTrigger(String jobName, String scheduleExp, HashMap<String, Object> param) {
		JobDetail jobDetail = buildJobDetail(jobName, param);
		return TriggerBuilder.newTrigger()
				.forJob(jobDetail)
				.withIdentity(jobDetail.getJobDataMap().getString("jobName").concat("Trigger"))
				.withSchedule(CronScheduleBuilder.cronSchedule(scheduleExp))
				.build();
	}

	private Trigger buildJobTrigger(String jobName, int value, HashMap<String, Object> param) {
		JobDetail jobDetail = buildJobDetail(jobName, param);
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(value).repeatForever();
		return TriggerBuilder
				.newTrigger()
				.forJob(jobDetail)
				.withIdentity(jobDetail.getJobDataMap().getString("jobName").concat("Trigger"))
				.withSchedule(scheduleBuilder)
				.build();
	}

	public JobDetail buildJobDetail(String name, HashMap<String, Object> params) {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.putAll(params);
		jobDataMap.put("jobName", name);

		return newJob(QuartzJob.class)
				.withIdentity(name, "batch")
				.usingJobData(jobDataMap)
				.storeDurably()
				.build();
	}



}