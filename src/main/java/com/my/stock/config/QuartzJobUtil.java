package com.my.stock.config;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuartzJobUtil {
	private static JobLauncher jobLauncher;
	public static JobLauncher getJobLauncher() {
		return jobLauncher;
	}
	public static void setJobLauncher(JobLauncher jobLauncher) {
		QuartzJobUtil.jobLauncher = jobLauncher;
	}

	private static JobLocator jobLocator;
	public static JobLocator getJobLocator() {
		return jobLocator;
	}
	public static void setJobLocator(JobLocator jobLocator) {
		QuartzJobUtil.jobLocator = jobLocator;
	}
	
//	private static List<Map<String, Trigger>> listBatchJob = new ArrayList<>();

	private static List<Trigger> triggers = new ArrayList<>();
	private static List<JobDetail> jobDetails = new ArrayList<>();

	public static List<Trigger> getTriggers() {
		return triggers;
	}

	public static List<JobDetail> getJobDetails() {
		return jobDetails;
	}

}