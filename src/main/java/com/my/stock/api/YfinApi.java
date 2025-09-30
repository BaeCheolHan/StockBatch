package com.my.stock.api;

import com.my.stock.dto.yfin.DividendsResponse;
import com.my.stock.dto.yfin.QuoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;
import com.my.stock.dto.yfin.HistoryResponse;

@FeignClient(name = "YfinApi", url = "${api.yfin.base-url:http://localhost:8080}")
public interface YfinApi {

    @GetMapping("/quote")
    QuoteDto getQuote(@RequestParam("ticker") String ticker,
                      @RequestParam(value = "exchange", required = false) String exchange);

    @GetMapping("/dividends")
    DividendsResponse getDividends(@RequestParam("ticker") String ticker,
                                   @RequestParam("range") String range,
                                   @RequestParam(value = "exchange", required = false) String exchange);

    @GetMapping("/history")
    HistoryResponse getHistory(@RequestParam("ticker") String ticker,
                      @RequestParam("range") String range,
                      @RequestParam("interval") String interval,
                      @RequestParam(value = "autoAdjust", defaultValue = "true") boolean autoAdjust);

    @GetMapping("/quotes")
    Map<String, Object> getQuotes(@RequestParam("tickers") String tickers);
}


