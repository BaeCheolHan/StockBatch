package com.my.stock.job;

import com.my.stock.base.BaseBatch;
import com.my.stock.dto.StockDividendHistory;
import com.my.stock.dto.kis.response.KrStockVolumeRankOutput;
import com.my.stock.rdb.entity.Stocks;
import com.my.stock.rdb.repository.StockRepository;
import com.my.stock.rdb.repository.StocksRepository;
import com.my.stock.redis.entity.DividendInfo;
import com.my.stock.redis.entity.KrStockVolumeRank;
import com.my.stock.redis.repository.DividendInfoRepository;
import com.my.stock.redis.repository.KrStockVolumeRankRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes2.HistoricalDividend;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class DividendDataSaveJobConfiguration extends BaseBatch {

	private final StocksRepository stocksRepository;

	private final StockRepository stockRepository;

	private final DividendInfoRepository dividendInfoRepository;

	private final KrStockVolumeRankRepository krStockVolumeRankRepository;

	private String targetSymbol;


	public DividendDataSaveJobConfiguration(DividendInfoRepository dividendInfoRepository, StocksRepository stocksRepository, StockRepository stockRepository, KrStockVolumeRankRepository krStockVolumeRankRepository) {
		super("DividendDataSaveJob", "0 0 * * * ?", null);

		this.dividendInfoRepository = dividendInfoRepository;
		this.stocksRepository = stocksRepository;
		this.stockRepository = stockRepository;
		this.krStockVolumeRankRepository = krStockVolumeRankRepository;
	}


	@Bean
	public Job DividendDataSaveJob(JobRepository jobRepository, Step dividendDataSaveJobStep) {
		return new JobBuilder("DividendDataSaveJob", jobRepository)
				.listener(jobListener)
				.start(dividendDataSaveJobStep)
				.build();
	}

	private final JobExecutionListener jobListener = new JobExecutionListener() {

		@Override
		public void beforeJob(JobExecution jobExecution) {
			targetSymbol = jobExecution.getJobParameters().getString("symbol");
		}

		@Override
		public void afterJob(JobExecution jobExecution) {

		}
	};

	@Bean
	public Step dividendDataSaveJobStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("dividendDataSaveJobStep", jobRepository)
				.<Stocks, DividendInfo>chunk(1, platformTransactionManager)
				.reader(stockSymbolReader)
				.processor(dividendDataProcessor)
				.writer(writer)
				.build();
	}

	private final ItemReader<Stocks> stockSymbolReader = new ItemReader<>() {
		List<Stocks> stockSymbols = null;

		@Override
		public Stocks read() throws Exception {
			if (stockSymbols == null) {
				if (targetSymbol != null) {
					stockSymbols = new ArrayList<>();
					stockSymbols.add(stocksRepository.findBySymbol(targetSymbol).orElseThrow(Exception::new));
				} else {
					List<DividendInfo> l = (List<DividendInfo>) dividendInfoRepository.findAll();
					List<String> symbols = stockRepository.findSymbolAllLeftJoinDividendGropBySymbol();

					Optional<KrStockVolumeRank> krStockVolumeRanks = krStockVolumeRankRepository.findById("0000");

					List<String> krStockVolumeRankSymbols = krStockVolumeRanks.stream()
							.map(it -> it.getData().getOutput().stream().map(KrStockVolumeRankOutput::getMksc_shrn_iscd).collect(Collectors.toList()))
							.flatMap(List::stream)
							.toList();

					List<String> joinedSymbols = Stream.of(symbols.stream(), l.stream().map(DividendInfo::getSymbol), krStockVolumeRankSymbols.stream())
							.flatMap(stringStream -> stringStream)
							.distinct()
							.toList();

					stockSymbols = stocksRepository.findAllBySymbolIn(joinedSymbols);
				}
			}

			if (stockSymbols.isEmpty()) {
				stockSymbols = null;
				return null;
			}

			return stockSymbols.remove(0);
		}
	};

	private final ItemProcessor<Stocks, DividendInfo> dividendDataProcessor = stocks -> {
		DividendInfo dividendInfo = new DividendInfo();
		Stock stock;
		try {
			String symbol = stocks.getSymbol();

			switch (stocks.getNational()) {
				case "KR":
					if (stocks.getCode().equals("KOSPI")) {
						symbol = symbol.concat(".KS");
					} else {
						symbol= symbol.concat(".KQ");
					}
					break;
				case "JP":
					symbol = symbol.concat(".T");
					break;
				case "CN":
					if(stocks.getCode().equals("SZS"))
						symbol = symbol.concat(".SZ");
					break;
				default:
					break;
			}

			stock = YahooFinance.get(symbol);

			dividendInfo.setSymbol(stocks.getSymbol());
			dividendInfo.setAnnualDividend(stock.getDividend().getAnnualYield());
			dividendInfo.setDividendRate(stock.getDividend().getAnnualYieldPercent());
			Calendar calendar = Calendar.getInstance();
			calendar.set(1970, Calendar.FEBRUARY, 1);

			List<HistoricalDividend> history = stock.getDividendHistory(calendar);
			if (history != null && !history.isEmpty()) {
				dividendInfo.setDividendHistories(history.stream().filter(it -> it.getDate() != null).map(it -> StockDividendHistory.builder()
						.symbol(stocks.getSymbol())
						.dividend(it.getAdjDividend())
						.date(it.getDateStr())
						.build()).toList());
			}

		} catch (Exception ignore) {
			ignore.printStackTrace();
			dividendInfo = new DividendInfo();
		}


		return dividendInfo;
	};


	private final ItemWriter<DividendInfo> writer = new ItemWriter<>() {
		@Override
		public void write(Chunk<? extends DividendInfo> chunk) {
			chunk.forEach(item -> {
				if (item.getSymbol() != null) {
					Optional<DividendInfo> entity = dividendInfoRepository.findById(item.getSymbol());
					entity.ifPresent(dividendInfoRepository::delete);
					dividendInfoRepository.save(item);
				}
			});
		}
	};

}
