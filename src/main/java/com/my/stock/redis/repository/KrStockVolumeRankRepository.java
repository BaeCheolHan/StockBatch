package com.my.stock.redis.repository;

import com.my.stock.redis.entity.KrStockVolumeRank;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KrStockVolumeRankRepository extends CrudRepository<KrStockVolumeRank, String> {
}
