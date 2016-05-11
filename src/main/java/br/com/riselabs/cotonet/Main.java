/**
 * 
 */
package br.com.riselabs.cotonet;

import java.io.File;
import java.io.IOException;
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
import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;
import br.com.riselabs.cotonet.model.exceptions.InvalidNumberOfTagsException;
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

		options.addOption(Option
				.builder("l")
				.longOpt("list")
				.desc("The path to the file containig the repository's URL of the target systems.")
				.hasArg().required().build());

		options.addOption(Option
				.builder("rw")
				.longOpt("rewrite-aux")
				.desc("Rewrite auxilary files (e.g., *.conf, *.sh) "
						+ "_WITHOUT_ "
						+ "the recreation of the merge scenarios based tags.")
				.hasArg(false).build());

		options.addOption(Option
				.builder("rwt")
				.longOpt("rewrite-tagfile")
				.desc("Rewrite auxilary files (e.g., *.conf, *.sh) "
						+ "_INCLUDING_ "
						+ "the recreation of the merge scenarios based tags.")
				.hasArg(false).build());

		options.addOption("h", "help", false, "Print this help page");

		File reposListFile = null;
		Boolean skipCloning = false;
		try {
			CommandLine cmd = parser.parse(options, args);

			// user is looking for help
			if (cmd.hasOption("h")) {
				new HelpFormatter().printHelp("java ", options);
				System.exit(0);
			}

			// the "l" option is required
			if (!cmd.hasOption("l")) {
				System.out
						.println("COTONET ended without retrive any repository.\n\n"
								+ "You should use 'h' if you are looking for help. Otherwise,"
								+ " the 'l' option is mandatory.");
				System.exit(1);
			} else {
				// running to download the repositories
				String urlsFilePath = cmd.getOptionValue("l");
				reposListFile = new File(urlsFilePath);

				// Ends execution if file not found.
				if (!reposListFile.exists()) {
					System.out
							.println("COTONET ended without retrive any repository.\n\n"
									+ "The file containig the repository's URL of the target systems was not found. "
									+ "Check wether the file \""
									+ urlsFilePath
									+ "\" exists.");
					System.exit(1);
				}

				skipCloning = (cmd.hasOption("rw") || cmd.hasOption("rwt")) ? true : false;

				// user is running to rewrite the auxiliary files
				if (skipCloning) {
					run(reposListFile, skipCloning);
					Logger.log("COTONET finished. Files rewritten.");
					System.exit(0);
				}

				run(reposListFile,skipCloning);
				
				Logger.log("COTONET finished. Files rewritten.");
			}

		} catch (ParseException e) {
			new HelpFormatter().printHelp("java ", options);
		} catch (Exception e) {
			Logger.log(e.getMessage());
		}
	}

	private static void run(File reposListFile, boolean skip) {
		IOHandler io = new IOHandler();
		// reponsable to coordinate the threads for each system
		RCThreadPoolExecutor pool = new RCThreadPoolExecutor();
		List<String> systems = io.readFile(reposListFile);
		
		for (String url : systems) {
			try {
				pool.runTask(new RepositoryCrawler(url, skip));
			} catch (IOException e) {
				Logger.logStackTrace(e);
			}
			Logger.log("Repository scheduled: " + url);
		}

		pool.shutDown();
	}


}
