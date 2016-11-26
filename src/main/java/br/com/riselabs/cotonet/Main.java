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
package br.com.riselabs.cotonet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import br.com.riselabs.cotonet.crawler.RepositoryCrawler;
import br.com.riselabs.cotonet.crawler.threads.RCThreadPoolExecutor;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;
import br.com.riselabs.cotonet.model.exceptions.InvalidNumberOfTagsException;
import br.com.riselabs.cotonet.util.CodefaceHelper;
import br.com.riselabs.cotonet.util.IOHandler;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class Main {

	/**
	 * @param args
	 * @throws EmptyContentException
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws InvalidNumberOfTagsException
	 */
	public static void main(String[] args) {

		CommandLineParser parser = new DefaultParser();
		Options options = new Options();

		options.addOption(Option.builder("c").longOpt("chunkBased")
				.desc("c - build a conflict chunk-based network with the developers that in fact conflits with each other."
						+ " Additionally to the c argument the user should provide a path. This path should have"
						+ " a file containig the repository's URL of the target systems.").hasArg().build());

		options.addOption(Option.builder("cf").longOpt("chunkBasedFullGraph")
				.desc("cf - like c, build a conflict chunk-based network adding all developers involved in "
						+ "identified chunk conflicts. Additionally to the cf argument the user should provide a path. "
						+ "This path should have a file containig the repository's URL of the target systems.").hasArg().build());

		options.addOption(Option.builder("f").longOpt("fileBase")
				.desc(" f - build a conflict file-based network. In other others all developers that contribute to some"
						+ " conflict at file level should be part of this network. This network is based on network provides "
						+ "by cf, adding edges between developers of different chunks. Additionally to the f argument the"
						+ " user should provide a path. This path should have afile containig the repository's URL of "
						+ "the target systems.").hasArg().build());
		/*
		 * options.addOption( Option.builder("rw").longOpt("rewrite-aux").
		 * desc("Rewrite auxilary files (e.g., *.conf, *.sh) " + "_WITHOUT_ " +
		 * "the recreation of the merge scenarios based tags.").hasArg(false).
		 * build());
		 * 
		 * options.addOption( Option.builder("rwt").longOpt("rewrite-tagfile").
		 * desc("Rewrite auxilary files (e.g., *.conf, *.sh) " + "_INCLUDING_ "
		 * + "the recreation of the merge scenarios based tags.").hasArg(false).
		 * build());
		 */
		options.addOption("h", "help", false, "Print this help page");

		File reposListFile = null;
		Boolean skipCloneAndNetworks = false;
		try {
			CommandLine cmd = parser.parse(options, args);
			// user is looking for help
			if (cmd.hasOption("h")) {
				new HelpFormatter().printHelp("java ", options);
				System.exit(0);
			}

			/* "c", "cf", and "f" are the three available options
			* "c" builds the chunk-based network with developers that contribute to the conflict
			* "cf" builds the chunk-based network with developers that contribute to the conflict and developers
			* that are part of the chunk, but don't contribute to the conflict
			* "f" builds the file-based network with developers that contribute to the chunk into a target file
			*/
			else if (cmd.hasOption("c") || cmd.hasOption("cf") || cmd.hasOption("f")) {

				String urlsFilePath = null;
				NetworkType type;
				if (cmd.hasOption("c")) {
					urlsFilePath = cmd.getOptionValue("c");
					type = NetworkType.CHUNK_BASED;
				} else if (cmd.hasOption("cf")) {
					urlsFilePath = cmd.getOptionValue("cf");
					type = NetworkType.CHUNK_BASED_FULL;
				} else {
					urlsFilePath = cmd.getOptionValue("f");
					type = NetworkType.FILE_BASED;
				} 

				System.out.println(urlsFilePath);

				reposListFile = new File(urlsFilePath);

				// Ends execution if file not found.
				if (!reposListFile.exists()) {
					System.out.println("COTONET ended without retrive any repository.\n\n"
							+ "The file containig the repository's URL of the target systems was not found. "
							+ "Check wether the file \"" + urlsFilePath + "\" exists.");
					System.exit(1);
				}

				skipCloneAndNetworks = (cmd.hasOption("rw") || cmd.hasOption("rwt")) ? true : false;

				MainThread m = new MainThread(type, reposListFile, skipCloneAndNetworks);
				m.start();
				m.join();
				Logger.log("COTONET finished. Files rewritten.");

			} else {
				System.out.println("COTONET ended without retrive any repository.\n\n"
						+ "You should use 'h' if you are looking for help. Otherwise,"
						+ " the 'l' or 'fc' option is mandatory.");
				System.exit(1);

			}

		} catch (ParseException e) {
			new HelpFormatter().printHelp("java ", options);
		} catch (Exception e) {
			Logger.log(e.getMessage());
		}
	}

	static class MainThread extends Thread {
		private File list;
		private boolean skip;
		private NetworkType type;

		public MainThread(NetworkType type, File reposListFile, boolean skipCloneAndNetworks) {
			this.list = reposListFile;
			this.skip = skipCloneAndNetworks;
			this.type = type;			
		}

		public void run() {
			IOHandler io = new IOHandler();
			// responsible to coordinate the threads for each system
			RCThreadPoolExecutor pool = new RCThreadPoolExecutor();
			List<String> systems = io.readFile(list);
			List<String> systems_name = new ArrayList<String>();

			for (String url : systems) {
				try {

					pool.runTask(new RepositoryCrawler(url, skip, type));
					
				} catch (IOException e) {
					Logger.logStackTrace(e);
				}
				Logger.log("Repository scheduled: " + url);
				String[] str = url.split("/");
				systems_name.add(str[str.length - 1]);
			}

			pool.shutDown();
			
			try {
				CodefaceHelper.createCodefaceRunScript(systems_name);
			} catch (NullPointerException | IOException | EmptyContentException e) {
				Logger.logStackTrace(e);
			}
		}

	}

}
