package com.my.stock.redis.repository;

import com.my.stock.redis.entity.KrNowStockPrice;
import com.my.stock.redis.entity.OverSeaNowStockPrice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OverSeaNowStockPriceRepository extends CrudRepository<OverSeaNowStockPrice, String> {
}
