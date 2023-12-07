
package yahoofinance.quotes.stock;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * All getters can return null in case the data is not available from Yahoo Finance.
 * 
 * @author Stijn Strickx
 */
public class StockDividend {
    
    @Getter
    private final String symbol;
    
    @Getter
    private Calendar payDate;
    @Getter
    private Calendar exDate;
    private BigDecimal annualYield;
    private BigDecimal annualYieldPercent;

    @Getter
    private BigDecimal dividendRate;

    @Getter
    private BigDecimal dividendYield;

    public StockDividend(String symbol) {
        this.symbol = symbol;
    }
    
    public StockDividend(String symbol, Calendar payDate, Calendar exDate, BigDecimal annualYield, BigDecimal annualYieldPercent, BigDecimal dividendRate, BigDecimal dividendYield) {
        this(symbol);
        this.payDate = payDate;
        this.exDate = exDate;
        this.annualYield = annualYield;
        this.annualYieldPercent = annualYieldPercent;
        this.dividendRate = dividendRate;
        this.dividendYield = dividendYield;
    }

    public void setPayDate(Calendar payDate) {
        this.payDate = payDate;
    }

    public void setExDate(Calendar exDate) {
        this.exDate = exDate;
    }
    
    public BigDecimal getAnnualYield() {
        return annualYield == null ? dividendYield : annualYield;
    }
    
    public void setAnnualYield(BigDecimal annualYield) {
        this.annualYield = annualYield;
    }
    
    public BigDecimal getAnnualYieldPercent() {
        return annualYieldPercent == null ? dividendRate : annualYieldPercent;
    }
    
    public void setAnnualYieldPercent(BigDecimal annualYieldPercent) {
        this.annualYieldPercent = annualYieldPercent;
    }


    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }

    public void setDividendRate(BigDecimal dividendRate) {
        this.dividendRate = dividendRate;
    }

    @Override
    public String toString() {
        String payDateStr = "/";
        String exDateStr = "/";
        String annualYieldStr = "/";
        if(this.payDate != null) {
            payDateStr = this.payDate.getTime().toString();
        }
        if(this.exDate != null) {
            exDateStr = this.exDate.getTime().toString();
        }
        if(this.annualYieldPercent != null) {
            annualYieldStr = this.annualYieldPercent.toString() + "%";
        }
        return "Pay date: " + payDateStr + ", Ex date: " + exDateStr + ", Annual yield: " + annualYieldStr;
    }
    
}
