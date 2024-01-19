package com.my.stock.job.stock.list.update.reader.custom;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.List;

public class CustomOverSeaStocksMultiResourceItemReader<Stocks> extends MultiResourceItemReader<Stocks> {

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		List<String> downloadFileList = (List<String>) executionContext.get("downloadFileList");
		List<FileSystemResource> files = new ArrayList<>();

		downloadFileList.forEach(fileName -> {
			if (fileName.contains(".COD")) files.add(new FileSystemResource("/tmp/".concat(fileName)));
		});
		super.setResources(files.toArray(FileSystemResource[]::new));

		super.open(executionContext);
	}
}
