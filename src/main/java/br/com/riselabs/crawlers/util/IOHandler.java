package br.com.riselabs.crawlers.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class IOHandler {

	public static List<String> readFile(File fin) {
		List<String> urls = null;
		try {
			FileInputStream fis = new FileInputStream(fin);

			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fis));

			urls = new ArrayList<String>();
			String line = null;
			while ((line = br.readLine()) != null) {
				urls.add(line);
			}

			br.close();

		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
		return urls;
	}

	public static List<String> readFile(Path filePath) {
		Charset charset = Charset.forName("US-ASCII");
		List<String> urls = null;
		try (BufferedReader reader = Files.newBufferedReader(filePath,
				charset)) {
			String line = null;
			urls = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				urls.add(line);
			}
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
		return urls;
	}
	
	
	public static void writeFile(File file, List<String> content) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));

			for (String line : content) {
				writer.write(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}
	
	public static File makeDirectory(String folderName) throws IOException {
		File localPath = new File(RCProperties.REPOS_DIR + folderName);
		if (localPath.exists()) {
			System.out.println("Cleaning directory:" + localPath.toString());
			FileUtils.deleteDirectory(localPath);
		}
		localPath.mkdirs();
		System.gc();
		return localPath;
	}
	
	public static String getRepositorySystemName(String remoteURL) {
		String[] path = remoteURL.split("/");
		return path[path.length - 1];
	}

	public static void writeTagsFile(String tagetSystemName, List<String> tags) {
		writeFile(new File(RCProperties.REPOS_DIR + tagetSystemName + "_TAGs.txt"), tags);
		
	}
}