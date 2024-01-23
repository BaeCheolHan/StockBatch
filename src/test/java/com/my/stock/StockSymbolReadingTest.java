package com.my.stock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StockSymbolReadingTest {

	@Test
	void testOverseaMsiFileRead() throws IOException {
		List<String> markets = Arrays.asList("nas","nys","ams","shs","shi","szs","szi","tse","hks","hnx","hsx");
		String downloadPath = "/tmp/";
		List<String> downloadFileList = new ArrayList<>();
		for(String market : markets) {
			String downloadFileName = downloadFileAndGetFileName(new URL(String.format("https://new.real.download.dws.co.kr/common/master/%smst.cod.zip", market)), downloadPath);
			if (downloadFileName.substring(downloadFileName.lastIndexOf(".")).equalsIgnoreCase(".zip")) {
				this.unzipFile(downloadPath, downloadFileName);
				downloadFileName = downloadFileName.replace(".zip", "");
				downloadFileList.add(downloadFileName.toUpperCase());
			}
		}


		for(String fileName : downloadFileList) {
			BufferedReader br = null;
			File mstFile = new File(downloadPath + fileName);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(mstFile), "cp949"));

			StringBuffer sb = new StringBuffer();

			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				String[] splitedLine = readLine.split("\t");
				String national = splitedLine[0];
				System.out.println(national);
				String symbol = splitedLine[4];
				System.out.println(symbol);
				String name = splitedLine[6];
				System.out.println(name);
				String currency = splitedLine[9];
				System.out.println(currency);
				String code = splitedLine[2];
				System.out.println(code);

				System.out.println("-------------------------------------------------------------------");
			}
			br.close();
		}


		File t1 = new File("./kosdaq_code.mst");
		File t2 = new File("./kosdaq_code.mst.zip");
		t1.delete();
		t2.delete();


	}

	@Test
	void testKosdaqMsiFileRead() throws IOException {

		String kosdaqCodeUrl = "https://new.real.download.dws.co.kr/common/master/kosdaq_code.mst.zip";
		String downloadPath = "/tmp/";
		String downloadFileName = downloadFileAndGetFileName(new URL(kosdaqCodeUrl), downloadPath);

		if (downloadFileName.substring(downloadFileName.lastIndexOf(".")).equalsIgnoreCase(".zip")) {
			this.unzipFile(downloadPath, downloadFileName);
			downloadFileName = downloadFileName.replace(".zip", "");
		}

		BufferedReader br = null;
		File mstFile = new File("/tmp/kosdaq_code.mst");
		br = new BufferedReader(new InputStreamReader(new FileInputStream(mstFile), "cp949"));

		StringBuffer sb = new StringBuffer();

		String readLine = null;
		while ((readLine = br.readLine()) != null) {
			String t1 = readLine.substring(0, readLine.length() - 222);
			String t2 = t1.substring(0, 9).trim();
			String t3 = t1.substring(9, 21);
			String t4 = t1.substring(21);

			System.out.println(t1);
			System.out.println(t2);
			System.out.println(t3);
			System.out.println(t4);
//			System.out.println(readLine.substring(readLine.length() - 222));
			System.out.println("-------------------------------------------------------------------");
		}
		br.close();

		File t1 = new File("./kosdaq_code.mst");
		File t2 = new File("./kosdaq_code.mst.zip");
		t1.delete();
		t2.delete();
	}

	@Test
	void testKospiMsiFileRead() throws IOException {

		String kosdaqCodeUrl = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";
		String downloadPath = "/tmp/";
		String downloadFileName = downloadFileAndGetFileName(new URL(kosdaqCodeUrl), downloadPath);

		if (downloadFileName.substring(downloadFileName.lastIndexOf(".")).equalsIgnoreCase(".zip")) {
			this.unzipFile(downloadPath, downloadFileName);
			downloadFileName = downloadFileName.replace(".zip", "");
		}

		BufferedReader br = null;
		File mstFile = new File("/tmp/kospi_code.mst");
		br = new BufferedReader(new InputStreamReader(new FileInputStream(mstFile), "cp949"));

		StringBuffer sb = new StringBuffer();

		String readLine = null;
		while ((readLine = br.readLine()) != null) {
			String t1 = readLine.substring(0, readLine.length() - 228);
			String t2 = t1.substring(0, 9).trim();
			String t3 = t1.substring(9, 21);
			String t4 = t1.substring(21);

			System.out.println(t1);
			System.out.println(t2);
			System.out.println(t3);
			System.out.println(t4);
//			System.out.println(readLine.substring(readLine.length() - 222));
			System.out.println("-------------------------------------------------------------------");
		}
		br.close();

		File t1 = new File("./kosdaq_code.mst");
		File t2 = new File("./kosdaq_code.mst.zip");
		t1.delete();
		t2.delete();
	}

	public void unzipFile(String filePath, String fileName) throws IOException {

		File zipFile = new File(filePath, fileName);
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry zipEntry = zis.getNextEntry();

		while (zipEntry != null) {
			File newFile = new File(filePath, zipEntry.getName());
			;
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
