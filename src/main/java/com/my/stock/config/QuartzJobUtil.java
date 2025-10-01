package com.my.stock.config;

import lombok.Getter;
import org.quartz.*;
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
	
	@Getter private static List<Trigger> triggers = new ArrayList<>();
	@Getter private static List<JobDetail> jobDetails = new ArrayList<>();

    public static class QuartzJobUtilHelper {
        public static JobDetail buildJobDetail(String jobName) {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("jobName", jobName);
            return JobBuilder.newJob(QuartzJob.class)
                    .withIdentity(jobName, "batch")
                    .usingJobData(jobDataMap)
                    .storeDurably()
                    .build();
        }

        public static Trigger buildCronTrigger(String jobName, String cron) {
            JobDetail jd = buildJobDetail(jobName);
            return TriggerBuilder.newTrigger()
                    .forJob(jd)
                    .withIdentity(jobName.concat("Trigger"))
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                            .withMisfireHandlingInstructionDoNothing())
                    .build();
        }
    }
}