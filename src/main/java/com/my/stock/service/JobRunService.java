package com.my.stock.service;

import com.my.stock.config.QuartzJobUtil;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.stereotype.Service;

@Service
public class JobRunService {
	public void runjob(String jobName, JobParameters jobParameter) {
		try {
			Job job = QuartzJobUtil.getJobLocator().getJob(jobName);
			QuartzJobUtil.getJobLauncher().run(job, jobParameter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}