package com.my.stock.config;

import lombok.Getter;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.ArrayList;
import java.util.List;

public class QuartzJobUtil {
	@Getter private static JobLauncher jobLauncher;

    public static void setJobLauncher(JobLauncher jobLauncher) {
		QuartzJobUtil.jobLauncher = jobLauncher;
	}

	@Getter private static JobLocator jobLocator;

    public static void setJobLocator(JobLocator jobLocator) {
		QuartzJobUtil.jobLocator = jobLocator;
	}
	
//	private static List<Map<String, Trigger>> listBatchJob = new ArrayList<>();

	@Getter private static List<Trigger> triggers = new ArrayList<>();
	@Getter private static List<JobDetail> jobDetails = new ArrayList<>();

}