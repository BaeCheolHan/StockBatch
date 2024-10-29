package com.my.stock;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class YahooFinanceTest {

    @Test
    public void getDataTest() throws IOException {
        Stock resp = YahooFinance.get("O");
        System.out.println(resp);
    }
}
