package com.my.stock.job;

import com.my.stock.service.YfinResilientClient;
import com.my.stock.config.BenchmarkConfigProperties;
import com.my.stock.config.ScheduledBatch;
import com.my.stock.dto.yfin.HistoryResponse;
import com.my.stock.rdb.entity.BenchmarkDailyReturn;
import com.my.stock.rdb.repository.BenchmarkDailyReturnRepository;
import com.my.stock.batch.listener.PersistenceClearChunkListener;
import com.my.stock.batch.listener.StepMetricsListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Configuration
@ScheduledBatch(job = "BenchmarkDailyJob", cron = "0 0/30 * * * ?")
public class BenchmarkJobsConfiguration {

    private final YfinResilientClient yfinClient;
	private final BenchmarkDailyReturnRepository repo;
	private final BenchmarkConfigProperties props;

    public BenchmarkJobsConfiguration(BenchmarkDailyReturnRepository repo, BenchmarkConfigProperties props, YfinResilientClient yfinClient) {
        this.repo = repo;
        this.props = props;
        this.yfinClient = yfinClient;
    }

	@Bean
	public Job benchmarkBackfillJob(JobRepository jobRepository, Step benchmarkBackfillStep) {
		return new JobBuilder("BenchmarkBackfillJob", jobRepository)
				.start(benchmarkBackfillStep)
				.build();
	}

	@Bean
	public Job benchmarkDailyJob(JobRepository jobRepository, Step benchmarkDailyStep) {
		return new JobBuilder("BenchmarkDailyJob", jobRepository)
				.start(benchmarkDailyStep)
				.build();
	}

	@Bean
	public Step benchmarkBackfillStep(JobRepository jobRepository, PlatformTransactionManager tm,
									  ItemReader<String> mergedBackfillTriggerReader,
									  ItemProcessor<String, List<BenchmarkDailyReturn>> mergedBackfillProcessor,
									  ItemWriter<List<BenchmarkDailyReturn>> benchmarkWriter,
									  StepMetricsListener stepMetricsListener,
									  PersistenceClearChunkListener persistenceClearChunkListener) {
		return new StepBuilder("benchmarkBackfillStep", jobRepository)
				.<String, List<BenchmarkDailyReturn>>chunk(1, tm)
				.reader(mergedBackfillTriggerReader)
				.processor(mergedBackfillProcessor)
				.writer(benchmarkWriter)
				.listener(stepMetricsListener)
				.listener(persistenceClearChunkListener)
				.faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(50).skip(Exception.class)
				.build();
	}

	@Bean
	public Step benchmarkDailyStep(JobRepository jobRepository, PlatformTransactionManager tm,
								   ItemReader<String> benchmarkDailyTriggerReader,
								   ItemProcessor<String, List<BenchmarkDailyReturn>> benchmarkDailyProcessor,
								   ItemWriter<List<BenchmarkDailyReturn>> benchmarkWriter,
								   StepMetricsListener stepMetricsListener,
								   PersistenceClearChunkListener persistenceClearChunkListener) {
		return new StepBuilder("benchmarkDailyStep", jobRepository)
				.<String, List<BenchmarkDailyReturn>>chunk(1, tm)
				.reader(benchmarkDailyTriggerReader)
				.processor(benchmarkDailyProcessor)
				.writer(benchmarkWriter)
				.listener(stepMetricsListener)
				.listener(persistenceClearChunkListener)
				.faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(50).skip(Exception.class)
				.build();
	}

	@Bean
	@StepScope
	public ItemReader<String> mergedBackfillTriggerReader() {
		return new ItemReader<>() {
			boolean emitted = false;
			@Override public String read() {
				if (emitted) return null;
				emitted = true;
				return "ALL";
			}
		};
	}

	@Bean
	@StepScope
	public ItemReader<String> benchmarkDailyTriggerReader() {
		return new ItemReader<>() {
			boolean emitted = false;
			@Override public String read() {
				if (emitted) return null;
				emitted = true;
				return "ALL";
			}
		};
	}

	@Bean
	@StepScope
	public ItemProcessor<String, List<BenchmarkDailyReturn>> mergedBackfillProcessor() {
		return trigger -> {
			List<String> codes = Optional.ofNullable(props.getCodes()).orElseGet(List::of);
			final LocalDate cutoff = LocalDate.of(2019, 12, 31);

			// 1) 수집: 심볼별 날짜->종가, 심볼별 cutoff 이전 최근 종가(seed), 전체 maxDate
			Map<String, String> displayByCode = new HashMap<>();
			Map<String, Map<LocalDate, BigDecimal>> closeBySymbol = new HashMap<>();
			Map<String, BigDecimal> seedClose = new HashMap<>();
			LocalDate maxDate = null;

			for (String code : codes) {
				String symbol = mapDisplaySymbol(code);
				displayByCode.put(code, symbol);
				// 백필은 seed 확보를 위해 range=6y로 넉넉히 조회
                HistoryResponse hr = yfinClient.getHistory(code, "6y", props.getHistory().getInterval(), props.getHistory().isAutoAdjust());
				if (hr == null || hr.getRows() == null || hr.getRows().isEmpty()) continue;
				Map<LocalDate, BigDecimal> map = closeBySymbol.computeIfAbsent(symbol, k -> new HashMap<>());
				// track latest before cutoff
				LocalDate latestBefore = null;
				BigDecimal latestBeforeClose = null;
				for (HistoryResponse.HistoryRow r : hr.getRows()) {
					LocalDate d = toKstDate(r.getTime());
					if (r.getClose() == null) continue;
					BigDecimal close = BigDecimal.valueOf(r.getClose());
					if (d.isBefore(cutoff)) {
						if (latestBefore == null || d.isAfter(latestBefore)) {
							latestBefore = d;
							latestBeforeClose = close;
						}
						continue;
					}
					map.put(d, close);
					if (maxDate == null || d.isAfter(maxDate)) maxDate = d;
				}
				if (latestBeforeClose != null) seedClose.put(symbol, latestBeforeClose);
			}

			if (maxDate == null) return List.of();

			// 2) 연속 날짜 범위 구성(cutoff..maxDate)
			List<LocalDate> allDates = new ArrayList<>();
			for (LocalDate d = cutoff; !d.isAfter(maxDate); d = d.plusDays(1)) allDates.add(d);

			// 3) 전 심볼에 대해 fill-forward(주말/공휴일 포함)
			List<BenchmarkDailyReturn> out = new ArrayList<>();
			for (String code : codes) {
				String symbol = displayByCode.get(code);
				if (symbol == null) continue;
				Map<LocalDate, BigDecimal> map = closeBySymbol.getOrDefault(symbol, Collections.emptyMap());
				BigDecimal prevClose = seedClose.get(symbol);
				BigDecimal cum = null;
				if (prevClose != null) cum = BigDecimal.valueOf(100);

				for (LocalDate d : allDates) {
					BigDecimal close = map.get(d);
					BigDecimal daily = null;
					if (close == null) {
						if (prevClose == null) {
							// 심볼의 첫 거래 이전엔 기준 종가가 없으므로 건너뜀
							continue;
						}
						close = prevClose;
						daily = BigDecimal.ZERO;
					} else {
						if (prevClose != null && prevClose.signum() != 0) {
							daily = close.divide(prevClose, 10, java.math.RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
						}
					}
					if (cum == null) cum = BigDecimal.valueOf(100);
					if (daily != null) cum = cum.multiply(BigDecimal.ONE.add(daily));

					BenchmarkDailyReturn e = new BenchmarkDailyReturn();
					e.setDate(d);
					e.setSymbol(symbol);
					e.setClose(close);
					e.setDailyReturn(daily);
					e.setCumIndex(cum);
					out.add(e);
					prevClose = close;
				}
			}

			out.sort(Comparator.comparing(BenchmarkDailyReturn::getDate).thenComparing(BenchmarkDailyReturn::getSymbol));
			return out;
		};
	}

	@Bean
	@StepScope
	public ItemProcessor<String, List<BenchmarkDailyReturn>> benchmarkDailyProcessor() {
		return trigger -> {
			List<String> codes = Optional.ofNullable(props.getCodes()).orElseGet(List::of);
			LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

			List<BenchmarkDailyReturn> out = new ArrayList<>();
			for (String code : codes) {
				String symbol = mapDisplaySymbol(code);
				// 최근 구간만 조회하여 부담 최소화
				HistoryResponse hr = yfinClient.getHistory(code, "1mo", props.getHistory().getInterval(), props.getHistory().isAutoAdjust());
				Map<LocalDate, BigDecimal> closeByDate = new HashMap<>();
				if (hr != null && hr.getRows() != null) {
					for (HistoryResponse.HistoryRow r : hr.getRows()) {
						if (r.getClose() == null) continue;
						LocalDate d = toKstDate(r.getTime());
						closeByDate.put(d, BigDecimal.valueOf(r.getClose()));
					}
				}

				Optional<BenchmarkDailyReturn> lastSavedOpt = repo.findTopBySymbolOrderByDateDesc(symbol);
				LocalDate start = lastSavedOpt.map(e -> e.getDate().plusDays(1)).orElse(today.minusDays(7));
				if (start.isAfter(today)) continue;

				BigDecimal prevClose = lastSavedOpt.map(BenchmarkDailyReturn::getClose).orElse(null);
				BigDecimal cum = lastSavedOpt.map(BenchmarkDailyReturn::getCumIndex).orElse(null);

				for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
					BigDecimal close = closeByDate.get(d);
					BigDecimal daily = null;
					if (close == null) {
						if (prevClose == null) continue; // 아직 시드 없음
						close = prevClose;
						daily = BigDecimal.ZERO;
					} else {
						if (prevClose != null && prevClose.signum() != 0) {
							daily = close.divide(prevClose, 10, java.math.RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
						}
					}
					if (cum == null) cum = BigDecimal.valueOf(100);
					if (daily != null) cum = cum.multiply(BigDecimal.ONE.add(daily));

					BenchmarkDailyReturn e = new BenchmarkDailyReturn();
					e.setDate(d);
					e.setSymbol(symbol);
					e.setClose(close);
					e.setDailyReturn(daily);
					e.setCumIndex(cum);
					out.add(e);
					prevClose = close;
				}
			}

			out.sort(Comparator.comparing(BenchmarkDailyReturn::getDate).thenComparing(BenchmarkDailyReturn::getSymbol));
			return out;
		};
	}

	@Bean
	@StepScope
	public ItemWriter<List<BenchmarkDailyReturn>> benchmarkWriter() {
        return (Chunk<? extends List<BenchmarkDailyReturn>> chunk) -> {
			for (List<BenchmarkDailyReturn> list : chunk) {
				for (BenchmarkDailyReturn e : list) {
                    repo.upsert(e);
				}
			}
		};
	}

	private static String mapDisplaySymbol(String raw) {
		if ("^KS11".equalsIgnoreCase(raw)) return "KOSPI";
		if ("^KQ11".equalsIgnoreCase(raw)) return "KOSDAQ";
		if ("^GSPC".equalsIgnoreCase(raw)) return "SNP500";
		if ("^IXIC".equalsIgnoreCase(raw)) return "NASDAQ";
		return raw;
	}

	private static LocalDate toKstDate(String isoTime) {
		ZonedDateTime zt = ZonedDateTime.parse(isoTime).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
		return zt.toLocalDate();
	}
}


