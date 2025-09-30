package com.my.stock.batch.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class StepMetricsListener implements StepExecutionListener {

    private long start;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        start = System.currentTimeMillis();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long elapsed = System.currentTimeMillis() - start;
        stepExecution.getExecutionContext().putLong("elapsedMs", elapsed);
        return stepExecution.getExitStatus();
    }
}


