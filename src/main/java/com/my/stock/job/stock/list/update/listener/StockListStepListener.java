package com.my.stock.job.stock.list.update.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Configuration
public class StockListStepListener {

	@Bean
	public StepExecutionListener krStockListStepListener() {
		return new StepExecutionListener() {
			@Override
			public void beforeStep(StepExecution stepExecution) {
				String fileUrl = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";

				if (stepExecution.getStepName().equalsIgnoreCase("KosdaqStockListUpdateStep")) {
					fileUrl = "https://new.real.download.dws.co.kr/common/master/kosdaq_code.mst.zip";
				}

				String downloadPath = "/tmp/";
				String downloadFileName;
				try {
					downloadFileName = downloadFileAndGetFileName(new URL(fileUrl), downloadPath);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				if (downloadFileName.substring(downloadFileName.lastIndexOf(".")).equalsIgnoreCase(".zip")) {
					try {
						unzipFile(downloadPath, downloadFileName);
					} catch (IOException e) {
					}
				}
			}

			@Override
			public ExitStatus afterStep(StepExecution stepExecution) {
				new File("/tmp/kosdaq_code.mst").delete();
				new File("/tmp/kospi_code.mst").delete();
				new File("/tmp/kosdaq_code.mst.zip").delete();
				new File("/tmp/kospi_code.mst.zip").delete();
				return ExitStatus.COMPLETED;
			}

		};
	}

	@Bean
	public StepExecutionListener overseaStockListStepListener() {
		List<String> downloadFileList = new ArrayList<>();

		return new StepExecutionListener() {
			@Override
			public void beforeStep(StepExecution stepExecution) {
				List<String> markets = Arrays.asList("nas", "nys", "ams", "shs", "shi", "szs", "szi", "tse", "hks", "hnx", "hsx");
				String downloadPath = "/tmp/";


				for (String market : markets) {
					String downloadFileName;
					try {
						downloadFileName = downloadFileAndGetFileName(new URL(String.format("https://new.real.download.dws.co.kr/common/master/%smst.cod.zip", market)), downloadPath);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					if (downloadFileName.substring(downloadFileName.lastIndexOf(".")).equalsIgnoreCase(".zip")) {
						try {
							unzipFile(downloadPath, downloadFileName);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						downloadFileName = downloadFileName.replace(".zip", "");
						downloadFileList.add(downloadFileName.toUpperCase());
					}

				}
				stepExecution.getExecutionContext().put("downloadFileList", downloadFileList);
			}

			@Override
			public ExitStatus afterStep(StepExecution stepExecution) {
				List<String> fileNames = (List<String>) stepExecution.getExecutionContext().get("downloadFileList");
				for(String filName : fileNames) {
					System.out.println(filName);
					new File("/tmp/".concat(filName)).delete();
					new File("/tmp/".concat(filName).concat(".zip")).delete();
				}
				return ExitStatus.COMPLETED;
			}
		};
	}


	public void unzipFile(String filePath, String fileName) throws IOException {

		File zipFile = new File(filePath, fileName);
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry zipEntry = zis.getNextEntry();

		while (zipEntry != null) {
			File newFile = new File(filePath, zipEntry.getName());

			if (zipEntry.isDirectory()) {
				if (!newFile.isDirectory() && !newFile.mkdirs()) {
					throw new IOException("Failed to create directory " + newFile);
				}
			} else {
				// fix for Windows-created archives
				File parent = newFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}
				byte[] buffer = new byte[4096];
				// write file content
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
			zipEntry = zis.getNextEntry();
		}
		zis.close();
	}


	public String downloadFileAndGetFileName(URL fileUrl, String downloadPath) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
		String fileName = "";
		String disposition = conn.getHeaderField("Content-Disposition");

		if (disposition != null) {
			String target = "filename=";
			int index = disposition.indexOf(target);
			if (index != -1) {
				fileName = disposition.substring(index + target.length() + 1);
			}
		} else {
			fileName = fileUrl.getPath().substring(fileUrl.getPath().lastIndexOf("/") + 1);
		}

		File zipFile = new File(downloadPath, fileName);

		InputStream is = conn.getInputStream();
		OutputStream os = new FileOutputStream(zipFile);

		final int BUFFER_SIZE = 4096;
		int bytesRead;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((bytesRead = is.read(buffer)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		os.close();
		is.close();
		return fileName;
	}
}
