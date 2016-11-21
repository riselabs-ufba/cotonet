/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alcemir R. Santos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.com.riselabs.cotonet.util;

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

import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;

/**
 * 
 * @author Alcemir R. Santos
 *
 */
public class IOHandler {

	public List<String> readFile(File fin) {
		List<String> lines = null;
		try (FileInputStream fis = new FileInputStream(fin);){
			
			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			lines = new ArrayList<String>();
			String line = null;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			br.close();

		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
		return lines;
	}

	public List<String> readFile(Path filePath) {
		Charset charset = Charset.forName("US-ASCII");
		List<String> urls = null;
		try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
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

	/**
	 * Appends a line to a given file.
	 * 
	 * @param file
	 * @param aLine
	 * @throws IOException
	 */
	public void appendLineToFile(File file, String aLine) throws IOException {
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(createFile(file), true));
		writer.write(aLine + "\n");
		// Close the writer regardless of what happens...
		writer.close();
	}

	/**
	 * Appends a list of lines to a given file.
	 * 
	 * @param file
	 * @param content
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws EmptyContentException
	 */
	public void writeFile(File file, List<String> content) throws IOException,
			NullPointerException, EmptyContentException {
		if (content == null)
			throw new NullPointerException();
		if (content.isEmpty())
			throw new EmptyContentException();

		createFile(file);
		for (String line : content) {
			appendLineToFile(file, line);
		}
	}

	/**
	 * Creates a file and all the inexistent parent folders if it not exists
	 * yet.
	 * 
	 * @param directory
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	private File createFile(File directory) throws IOException {
		if (!directory.getParentFile().exists()) {
			directory.getParentFile().mkdirs();
		}
		directory.createNewFile();
		return directory;
	}

	/**
	 * Checks whether the directory correspondent to the given name already
	 * exists. Returns <code>null</code> if not.
	 * 
	 * @param folderName
	 * @return - the directory
	 * @throws IOException
	 */
	public File getDirectory(File dir) throws IOException {
		if (dir.exists()) {
			return dir;
		}
		return null;
	}

	/**
	 * Rewrite the repository folder for a given target system.
	 * 
	 * @param sytemName
	 * @return
	 * @throws IOException
	 */
	public File makeSystemDirectory(String sytemName) throws IOException {
		File systemDir = new File(Directories.getReposDir(), sytemName);
		checkAndRemove(systemDir);
		systemDir.mkdirs();
		System.gc();
		return systemDir;
	}

	/**
	 * Checks and removes either a file or a directory. No exceptions raised if
	 * it fails.
	 * 
	 * @param instance
	 * @throws IOException
	 */
	public void checkAndRemove(File instance) throws IOException {
		if (getDirectory(instance) != null) {
			System.out.println("Removing old file: " + instance.toString());
			FileUtils.deleteQuietly(instance);
		}
	}

}