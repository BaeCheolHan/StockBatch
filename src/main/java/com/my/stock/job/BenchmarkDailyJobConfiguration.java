package com.my.stock.job;

import com.my.stock.api.YfinApi;
import com.my.stock.base.BaseBatch;
import com.my.stock.config.BenchmarkConfigProperties;
import com.my.stock.dto.yfin.HistoryResponse;
import com.my.stock.rdb.entity.BenchmarkDailyReturn;
import com.my.stock.rdb.repository.BenchmarkDailyReturnRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Configuration
public class BenchmarkDailyJobConfiguration extends BaseBatch {

    private final YfinApi yfinApi;
    private final BenchmarkDailyReturnRepository repo;
    private final BenchmarkConfigProperties props;

    public BenchmarkDailyJobConfiguration(YfinApi yfinApi, BenchmarkDailyReturnRepository repo, BenchmarkConfigProperties props) {
        super("BenchmarkDailyJob", props.getSchedule() != null ? props.getSchedule().getDailyCron() : "0 30 0 * * ?", null);
        this.yfinApi = yfinApi;
        this.repo = repo;
        this.props = props;
    }

    @Bean
    public Job BenchmarkDailyJob(JobRepository jobRepository, Step benchmarkDailyStep) {
        return new JobBuilder("BenchmarkDailyJob", jobRepository)
                .start(benchmarkDailyStep)
                .build();
    }

    @Bean
    public Step benchmarkDailyStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                   ItemReader<String> benchmarkDailyCodeReader,
                                   ItemProcessor<String, List<BenchmarkDailyReturn>> benchmarkDailyProcessor,
                                   ItemWriter<List<BenchmarkDailyReturn>> benchmarkDailyWriter) {
        return new StepBuilder("benchmarkDailyStep", jobRepository)
                .<String, List<BenchmarkDailyReturn>>chunk(4, tm)
                .reader(benchmarkDailyCodeReader)
                .processor(benchmarkDailyProcessor)
                .writer(benchmarkDailyWriter)
                .faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(10).skip(Exception.class)
                .build();
    }

    @Bean
    public ItemReader<String> benchmarkDailyCodeReader() {
        return new ItemReader<>() {
            int idx = 0;
            @Override public String read() {
                List<String> codes = props.getCodes();
                if (codes == null || idx >= codes.size()) return null;
                return codes.get(idx++);
            }
        };
    }

    @Bean
    public ItemProcessor<String, List<BenchmarkDailyReturn>> benchmarkDailyProcessor() {
        return code -> {
            // 최근 2일만 조회해서 전일 대비 금일 수익률 계산
            HistoryResponse hr = yfinApi.getHistory(code, "2d", "1d", true);
            if (hr == null || hr.getRows() == null || hr.getRows().isEmpty()) return List.of();
            List<HistoryResponse.HistoryRow> rows = new ArrayList<>(hr.getRows());
            rows.sort(Comparator.comparing(r -> toKstDate(r.getTime())));

            List<BenchmarkDailyReturn> out = new ArrayList<>();
            BigDecimal prevClose = null;
            for (HistoryResponse.HistoryRow r : rows) {
                LocalDate d = toKstDate(r.getTime());
                BigDecimal close = r.getClose() == null ? null : BigDecimal.valueOf(r.getClose());
                if (close == null) continue;
                BigDecimal daily = null;
                if (prevClose != null && prevClose.signum() != 0) {
                    daily = close.divide(prevClose, 10, java.math.RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
                }
                BenchmarkDailyReturn e = new BenchmarkDailyReturn();
                e.setDate(d);
                e.setSymbol(mapDisplaySymbol(code));
                e.setClose(close);
                e.setDailyReturn(daily);
                out.add(e);
                prevClose = close;
            }

            // 누적지수 갱신: 저장소에서 전일 값 가져와 계산
            for (BenchmarkDailyReturn e : out) {
                repo.findTopBySymbolAndDateLessThanOrderByDateDesc(e.getSymbol(), e.getDate())
                        .ifPresentOrElse(prev -> {
                            BigDecimal prevCum = prev.getCumIndex() != null ? prev.getCumIndex() : BigDecimal.valueOf(100);
                            BigDecimal daily = e.getDailyReturn() != null ? e.getDailyReturn() : BigDecimal.ZERO;
                            e.setCumIndex(prevCum.multiply(BigDecimal.ONE.add(daily)));
                        }, () -> {
                            // 없으면 초기화
                            BigDecimal daily = e.getDailyReturn() != null ? e.getDailyReturn() : BigDecimal.ZERO;
                            e.setCumIndex(BigDecimal.valueOf(100).multiply(BigDecimal.ONE.add(daily)));
                        });
            }
            return out;
        };
    }

    @Bean
    public ItemWriter<List<BenchmarkDailyReturn>> benchmarkDailyWriter() {
        return list -> {
            for (List<BenchmarkDailyReturn> l : list) {
                for (BenchmarkDailyReturn e : l) {
                    repo.findTopBySymbolOrderByDateDesc(e.getSymbol())
                            .filter(prev -> prev.getDate().isEqual(e.getDate()))
                            .ifPresentOrElse(prev -> {
                                prev.setClose(e.getClose());
                                prev.setDailyReturn(e.getDailyReturn());
                                prev.setCumIndex(e.getCumIndex());
                                repo.save(prev);
                            }, () -> repo.save(e));
                }
            }
        };
    }

    private static LocalDate toKstDate(String iso) {
        ZonedDateTime zt = ZonedDateTime.parse(iso).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        return zt.toLocalDate();
    }
    private static String mapDisplaySymbol(String raw) {
        if ("^KS11".equalsIgnoreCase(raw)) return "KOSPI";
        if ("^KQ11".equalsIgnoreCase(raw)) return "KOSDAQ";
        if ("^GSPC".equalsIgnoreCase(raw)) return "SNP500";
        if ("^IXIC".equalsIgnoreCase(raw)) return "NASDAQ";
        return raw;
    }
}


