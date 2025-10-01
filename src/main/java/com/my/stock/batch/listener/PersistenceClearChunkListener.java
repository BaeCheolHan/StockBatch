package com.my.stock.batch.listener;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
public class PersistenceClearChunkListener implements ChunkListener {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void beforeChunk(ChunkContext context) { }

    @Override
    public void afterChunk(ChunkContext context) {
        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }
    }

    @Override
    public void afterChunkError(ChunkContext context) { }
}


