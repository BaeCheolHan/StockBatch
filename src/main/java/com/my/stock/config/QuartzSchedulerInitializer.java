package com.my.stock.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzSchedulerInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final Scheduler scheduler;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // 1) JobDetails 등록
            for (JobDetail jobDetail : QuartzJobUtil.getJobDetails()) {
                if (!scheduler.checkExists(jobDetail.getKey())) {
                    scheduler.addJob(jobDetail, true);
                }
            }

            // 2) Triggers 등록/갱신
            for (Trigger trigger : QuartzJobUtil.getTriggers()) {
                TriggerKey tk = trigger.getKey();
                if (scheduler.checkExists(tk)) {
                    scheduler.rescheduleJob(tk, trigger);
                } else {
                    // 해당 트리거가 참조하는 잡키가 존재해야 함
                    JobKey jobKey = trigger.getJobKey();
                    if (!scheduler.checkExists(jobKey)) {
                        log.warn("Referenced job does not exist for trigger: {}. Scheduling job+trigger together.", tk);
                        // 동일 key의 JobDetail을 찾아 함께 등록
                        JobDetail jd = QuartzJobUtil.getJobDetails().stream()
                                .filter(j -> j.getKey().equals(jobKey))
                                .findFirst()
                                .orElse(null);
                        if (jd != null) {
                            scheduler.scheduleJob(jd, trigger);
                            continue;
                        }
                    }
                    scheduler.scheduleJob(trigger);
                }
            }
        } catch (SchedulerException e) {
            log.error("Failed to initialize Quartz jobs/triggers", e);
        }
    }
}


