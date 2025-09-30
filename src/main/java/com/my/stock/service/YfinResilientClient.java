package com.my.stock.service;

import com.my.stock.api.YfinApi;
import com.my.stock.dto.yfin.HistoryResponse;
import com.my.stock.dto.yfin.QuoteDto;
import com.my.stock.dto.yfin.DividendsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YfinResilientClient {

    private final YfinApi yfinApi;

    public HistoryResponse getHistory(String ticker, String range, String interval, boolean autoAdjust) {
        return yfinApi.getHistory(ticker, range, interval, autoAdjust);
    }

    public QuoteDto getQuote(String ticker, String exchange) {
        return yfinApi.getQuote(ticker, exchange);
    }

    public DividendsResponse getDividends(String ticker, String range, String exchange) {
        return yfinApi.getDividends(ticker, range, exchange);
    }
}


