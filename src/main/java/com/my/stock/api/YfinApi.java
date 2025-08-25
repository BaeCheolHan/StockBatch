package com.my.stock.api;

import com.my.stock.dto.yfin.DividendsResponse;
import com.my.stock.dto.yfin.QuoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "YfinApi", url = "${api.yfin.base-url:http://localhost:8080}")
public interface YfinApi {

    @GetMapping("/quote")
    QuoteDto getQuote(@RequestParam("ticker") String ticker,
                      @RequestParam(value = "exchange", required = false) String exchange);

    @GetMapping("/dividends")
    DividendsResponse getDividends(@RequestParam("ticker") String ticker,
                                   @RequestParam("range") String range,
                                   @RequestParam(value = "exchange", required = false) String exchange);
}


