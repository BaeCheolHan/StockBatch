package com.my.stock.job.stock.list.update.writer;

import com.my.stock.rdb.entity.Stocks;
import com.my.stock.rdb.repository.StocksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StocksItemWriterConfig {
	private final StocksRepository stocksRepository;

	@Bean
	public ItemWriter<Stocks> stocksItemWriter() {
		return chunk -> stocksRepository.saveAll(chunk);
	}

	@Bean
	public ItemWriter<Stocks> printstocksItemWriter() {
		return System.out::println;
	}
}
