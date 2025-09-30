package com.my.stock.batch.policy;

import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.ExhaustedRetryException;

public class RestRetrySkipPolicy implements SkipPolicy {
    @Override
    public boolean shouldSkip(Throwable t, long skipCount) {
        // 4xx/무결성 예외는 스킵, 재시도 고갈도 스킵 처리
        if (t instanceof DataIntegrityViolationException) return true;
        if (t instanceof FlatFileParseException) return true;
        if (t instanceof ExhaustedRetryException) return true;
        return false;
    }
}


