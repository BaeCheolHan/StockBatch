package com.my.stock.dto;

import com.my.stock.redis.entity.OverSeaNowStockPrice;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverSeaNowStockPriceWrapper {
    private OverSeaNowStockPrice output;
    private String rt_cd;
    private String msg_cd;
    private String msg1;
}
