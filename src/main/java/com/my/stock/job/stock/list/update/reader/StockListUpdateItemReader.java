package com.my.stock.job.stock.list.update.reader;

import com.my.stock.job.stock.list.update.reader.custom.CustomOverSeaStocksMultiResourceItemReader;
import com.my.stock.rdb.entity.Stocks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockListUpdateItemReader {


	@Bean
	public FlatFileItemReader<Stocks> kosdaqStocksItemReader() {
		FlatFileItemReader<Stocks> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setStrict(false);
		flatFileItemReader.setEncoding("cp949");

		flatFileItemReader.setResource(new FileSystemResource("/tmp/kosdaq_code.mst"));
		flatFileItemReader.setLineMapper((line, lineNumber) -> {
			String infoLine = line.substring(0, line.length() - 222);
			String symbol = infoLine.substring(0, 9).trim();
			String generalCode = infoLine.substring(9, 21);
			String name = infoLine.substring(21);

			Stocks stocks = new Stocks();
			stocks.setSymbol(symbol);
			stocks.setCode("KOSDAQ");
			stocks.setName(name);
			stocks.setNational("KR");
			stocks.setCurrency("KRW");

			return stocks;
		});

		return flatFileItemReader;
	}

	@Bean
	public FlatFileItemReader<Stocks> kospiStocksItemReader() {
		FlatFileItemReader<Stocks> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setStrict(false);
		flatFileItemReader.setEncoding("cp949");

		flatFileItemReader.setResource(new FileSystemResource("/tmp/kospi_code.mst"));
		flatFileItemReader.setLineMapper((line, lineNumber) -> {
			String infoLine = line.substring(0, line.length() - 228);
			String symbol = infoLine.substring(0, 9).trim();
			String generalCode = infoLine.substring(9, 21);
			String name = infoLine.substring(21);

			Stocks stocks = new Stocks();
			stocks.setSymbol(symbol);
			stocks.setCode("KOSPI");
			stocks.setName(name);
			stocks.setNational("KR");
			stocks.setCurrency("KRW");

			return stocks;
		});

		return flatFileItemReader;
	}


	@Bean
	public FlatFileItemReader<Stocks> customerItemReader() {
		FlatFileItemReader<Stocks> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setStrict(false);
		flatFileItemReader.setEncoding("cp949");
		flatFileItemReader.setLineMapper(((line, lineNumber) -> {
			final String[] splitedLine = line.split("\t");
			return Stocks.builder()
					.symbol(splitedLine[4])
					.code(splitedLine[2])
					.currency(splitedLine[9])
					.name(splitedLine[6])
					.national(splitedLine[0])
					.build();
		}));
		return flatFileItemReader;
	}

	@Bean
	public MultiResourceItemReader<Stocks> overseaStockItemReader(FlatFileItemReader<Stocks> customerItemReader) {
		CustomOverSeaStocksMultiResourceItemReader<Stocks> reader = new CustomOverSeaStocksMultiResourceItemReader<>();
		reader.setDelegate(customerItemReader);
		reader.setStrict(false);
		return reader;
	}
}
