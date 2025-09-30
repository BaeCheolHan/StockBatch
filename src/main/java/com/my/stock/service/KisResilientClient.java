package com.my.stock.service;

import com.my.stock.api.KisApi;
import com.my.stock.dto.KrNowStockPriceWrapper;
import com.my.stock.dto.OverSeaNowStockPriceWrapper;
import com.my.stock.dto.kis.request.KrStockPriceRequest;
import com.my.stock.dto.kis.request.OverSeaStockPriceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KisResilientClient {

    private final KisApi kisApi;

    public OverSeaNowStockPriceWrapper getOverSeaStockPrice(HttpHeaders header, OverSeaStockPriceRequest param) {
        return kisApi.getOverSeaStockPrice(header, param);
    }

    public KrNowStockPriceWrapper getKorStockPrice(HttpHeaders header, KrStockPriceRequest param) {
        return kisApi.getKorStockPrice(header, param);
    }
}


