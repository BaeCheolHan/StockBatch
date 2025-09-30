package com.my.stock.config;

import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledBatchRegistrar {

    private final ApplicationContext applicationContext;

    @PostConstruct
    public void register() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ScheduledBatch.class);
        beans.forEach((name, bean) -> {
            ScheduledBatch meta = bean.getClass().getAnnotation(ScheduledBatch.class);
            if (meta == null) return;
            String jobName = meta.job();
            String cron = meta.cron();
            QuartzJobUtil.getJobDetails().add(QuartzJobUtil.QuartzJobUtilHelper.buildJobDetail(jobName));
            QuartzJobUtil.getTriggers().add(QuartzJobUtil.QuartzJobUtilHelper.buildCronTrigger(jobName, cron));
        });
    }
}


