package com.my.stock.service;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class QuartzCronService {

    private final Scheduler scheduler;

    public void rescheduleCron(String triggerName, String triggerGroup, String cronExpression) {
        try {
            String group = (triggerGroup == null || triggerGroup.isBlank()) ? "DEFAULT" : triggerGroup;
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, group);
            Trigger old = scheduler.getTrigger(triggerKey);
            if (old == null) {
                throw new RuntimeException("Trigger not found: " + group + "." + triggerName);
            }
            JobKey jobKey = old.getJobKey();

            CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule(cronExpression)
                    .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            CronTrigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobKey)
                    .withSchedule(schedule)
                    .build();

            scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}


