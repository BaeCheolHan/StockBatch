package com.my.stock.redis.entity;

import com.my.stock.dto.kis.response.KrStockVolumeRankWrapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@SuperBuilder
@Getter
@Setter
@RedisHash("KrStockVolumeRank")
public class KrStockVolumeRank {
	@Id
	private String id;

	private KrStockVolumeRankWrapper data;

	@TimeToLive(unit = TimeUnit.MINUTES)
	@Builder.Default
	private Long expiration = 3L;
}
