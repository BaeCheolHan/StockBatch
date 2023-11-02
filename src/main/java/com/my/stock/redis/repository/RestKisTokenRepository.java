package com.my.stock.redis.repository;


import com.my.stock.redis.entity.RestKisToken;
import org.springframework.data.repository.CrudRepository;

public interface RestKisTokenRepository extends CrudRepository<RestKisToken, String> {
}
