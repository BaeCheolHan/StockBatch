package com.my.stock.redis.repository;

import com.my.stock.redis.entity.DividendInfo;
import com.my.stock.redis.entity.RestEbestToken;
import org.springframework.data.repository.CrudRepository;

public interface DividendInfoRepository extends CrudRepository<DividendInfo, String> {
}
