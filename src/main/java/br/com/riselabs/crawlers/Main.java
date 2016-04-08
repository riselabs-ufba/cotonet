/**
 * 
 */
package br.com.riselabs.crawlers;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.crawlers.beans.MergeScenario;
import br.com.riselabs.crawlers.core.ReposCrawler;
import br.com.riselabs.crawlers.core.threads.CrawlerThread;
import br.com.riselabs.crawlers.core.threads.RCThreadPoolExecutor;
import br.com.riselabs.crawlers.db.DBManager;
import br.com.riselabs.crawlers.exceptions.EmptyContentException;
import br.com.riselabs.crawlers.exceptions.InvalidNumberOfTagsException;
import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.util.RCUtil;

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
		Boolean rwTags = false;
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
						.println("ReposCrawler ended without retrive the repositories. "
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
							.println("ReposCrawler ended without retrive the repositories. "
									+ "The file containig the repository's URL of the target systems was not found. "
									+ "Check wether the file \""
									+ urlsFilePath
									+ "\" exists.");
					System.exit(1);
				}

				// TODO remove workaround
				if (System.getProperty("os.name").equals("Mac OS X")) {
					// it is running locally
					RCProperties.setWorkingDir(File.separator + "Documents"
							+ File.separator + "Workspace");
				} else {
					// it is running @ tindao
					RCProperties.setWorkingDir();
				}

				rwTags = cmd.hasOption("rwt") ? true : false;

				// user is running to rewrite the auxiliary files
				if (cmd.hasOption("rw") || cmd.hasOption("rwt")) {
					File tmpLogFile = new File(RCProperties.getLogDir()
							+ File.separator + "repos_crawler.log");
					RCUtil.setLog(tmpLogFile);
					rewriteAuxFiles(reposListFile, rwTags);
					RCUtil.log("ReposCrawler finished. Files rewritten.");
					System.exit(0);
				}

				run(reposListFile);
			}

		} catch (ParseException e) {
			new HelpFormatter().printHelp("java ", options);
		} catch (NullPointerException e) {
			RCUtil.log(e.getMessage());
		} catch (IOException e) {
			RCUtil.log(e.getMessage());
		} catch (EmptyContentException e) {
			RCUtil.log(e.getMessage());
		} catch (InvalidNumberOfTagsException e) {
			RCUtil.log(e.getMessage());
		}
	}

	private static void run(File reposListFile) throws IOException,
			NullPointerException, EmptyContentException {
		IOHandler io = new IOHandler();
		File reposCrawlerLogFile = new File(RCProperties.getLogDir()
				+ File.separator + "repos_crawler.log");
		io.checkAndRemove(reposCrawlerLogFile);
		RCUtil.setLog(reposCrawlerLogFile);

		List<String> target_systems = new ArrayList<String>();

		Map<Integer, String> reposURLs = readRepositoryURLs("txt",
				reposListFile);

		int count = 1;
		io.makeDirectory(new File(RCProperties.getLogDir()));

		RCThreadPoolExecutor pool = new RCThreadPoolExecutor();

		// for each url in the list clones the repository
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			String system = RCUtil.getRepositorySystemName(anEntry.getValue());
			try {
				pool.runTask(new CrawlerThread(count, system, anEntry));
				target_systems.add(system);
			} catch (Exception e) {
				RCUtil.logStackTrace(e);
			}
			count++;
		}

		pool.shutDown();

		io.createCodefaceRunScript(target_systems);
	}

	/**
	 * This method should be used to rewrite the auxiliary files (i.e. *.conf,
	 * *TAGsMapping.txt).
	 * 
	 * @param urlsFile
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws EmptyContentException
	 * @throws InvalidNumberOfTagsException
	 */
	public static void rewriteAuxFiles(File urlsFile, Boolean rwTags)
			throws IOException, NullPointerException, EmptyContentException,
			InvalidNumberOfTagsException {
		List<String> target_systems = new ArrayList<String>();

		// read repos
		Map<Integer, String> reposURLs = readRepositoryURLs("txt", urlsFile);
		RCThreadPoolExecutor pool = new RCThreadPoolExecutor();

		// for each repo write files
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			String system = RCUtil.getRepositorySystemName(anEntry.getValue());

			target_systems.add(system);

			pool.runTask(new Runnable() {

				@Override
				public void run() {
					try {
						IOHandler io = new IOHandler();
						ReposCrawler crawler = new ReposCrawler();

						crawler.setRepositoryID(anEntry.getKey());
						crawler.setRepositoryURL(anEntry.getValue());

						crawler.setWorkDir(io.getReposDirectory(system));
						Repository repository = crawler.getRepository();

						if (rwTags == true) {
							crawler.createMergeBasedTags();
						}

						List<String> lines = new ArrayList<String>();

						try (Git git = new Git(repository)) {
							RevWalk revWalk = new RevWalk(repository);

							System.out
									.println("Writing Tags Mapping File for \""
											+ system + "\".");
							for (Entry<String, Ref> e : repository.getTags()
									.entrySet()) {
								RevCommit rc = revWalk.parseCommit(e.getValue()
										.getObjectId());
								lines.add(e.getKey() + ": " + rc.getName());
							}

							File f = new File(RCProperties.getReposDir()
									+ system + "_TAGsMapping.txt");
							if (f.exists())
								f.delete();
							io.writeFile(f, lines);

							System.out
									.println("Writing codeface configuration file for \""
											+ system + "\".");
							io.createCodefaceConfFiles(system, repository
									.getTags().size());
							revWalk.close();
						} catch (NullPointerException | EmptyContentException
								| InvalidNumberOfTagsException e1) {
							RCUtil.logStackTrace(e1);
							e1.printStackTrace();
						}
					} catch (IOException e) {
						RCUtil.logStackTrace(e);
						e.printStackTrace();
					}

				}
			});

		}
		pool.shutDown();
		System.out.println("Writing shell script to run the target systems.");
		new IOHandler().createCodefaceRunScript(target_systems);
	}

	/**
	 * This method should be used to rewrite the auxiliary files (i.e. *.conf,
	 * *TAGsMapping.txt).
	 * 
	 * @param urlsFile
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws EmptyContentException
	 * @throws InvalidNumberOfTagsException
	 */
	public static void rewriteAuxFiles2(File urlsFile, Boolean rwTags)
			throws IOException, NullPointerException, EmptyContentException,
			InvalidNumberOfTagsException {
		List<String> target_systems = new ArrayList<String>();

		// read repos
		Map<Integer, String> reposURLs = readRepositoryURLs("txt", urlsFile);
		RCThreadPoolExecutor pool = new RCThreadPoolExecutor();

		// for each repo write files
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			String system = RCUtil.getRepositorySystemName(anEntry.getValue());

			target_systems.add(system);

			pool.runTask(new Runnable() {

				@Override
				public void run() {
					try {
						IOHandler io = new IOHandler();

						List<MergeScenario> scenarios = IOHandler
								.getMergeScenarios(anEntry.getKey());

						System.out.println("Writing Tags Mapping File for \""
								+ system + "\".");
						int count = 1;
						Map<String, String> tagsMap = new HashMap<String, String>();
						for (MergeScenario s : scenarios) {
							String tagB = system + "B" + count;
							String tagL = system + "L" + count;
							String tagR = system + "R" + count;

							tagsMap.put(tagB, s.getBase());
							tagsMap.put(tagL, s.getLeft());
							tagsMap.put(tagR, s.getRight());
						}

						List<String> lines = new ArrayList<String>();
						for (Entry<String, String> e : tagsMap.entrySet()) {
							lines.add(e.getKey() + ":" + e.getValue());
						}
						String filePath = RCProperties.getReposDir() + system
								+ "_TAGsMapping.txt";
						File f = new File(filePath);
						if (f.exists())
							f.delete();
						io.writeFile(new File(filePath), lines);

						System.out
								.println("Writing codeface configuration file for \""
										+ system + "\".");
						io.createCodefaceConfFiles(system, tagsMap.size());

					} catch (IOException e) {
						RCUtil.logStackTrace(e);
						e.printStackTrace();
					} catch (NullPointerException e2) {
						RCUtil.logStackTrace(e2);
						e2.printStackTrace();
					} catch (EmptyContentException e2) {
						RCUtil.logStackTrace(e2);
						e2.printStackTrace();
					} catch (InvalidNumberOfTagsException e2) {
						RCUtil.logStackTrace(e2);
						e2.printStackTrace();
					}

				}
			});

		}
		pool.shutDown();
		System.out.println("Writing shell script to run the target systems.");
		new IOHandler().createCodefaceRunScript(target_systems);
	}

	/**
	 * use "db" to recover from the database and "txt" to recover from the file.
	 * <p>
	 * Ex: <code>readRepositoryURLs("txt", reposListFile);</code> or
	 * <code>readRepositoryURLs("db", null)</code>
	 * 
	 * @return - a map from the repository id in the ghanalysis db to the url.
	 */
	private static Map<Integer, String> readRepositoryURLs(String source,
			File urlsFile) {
		Map<Integer, String> reposURLs = null;
		System.out.print("Reading URLs from: " + urlsFile.toString());

		switch (source) {
		case "txt": // It is a test. It should read from te .txt file.
			reposURLs = new HashMap<Integer, String>();
			List<String> l = new IOHandler().readFile(urlsFile.toPath());

			for (int i = 1; i <= l.size(); i++) {
				try {
					ResultSet rs = DBManager
							.executeQuery("select r.id id from repository r where r.url=\'"
									+ l.get(i - 1).replace("https:", "http:")
									+ "\'");
					if (!rs.isBeforeFirst()) {
						System.out.println("[Skipping "
								+ RCUtil.getRepositorySystemName(l.get(i - 1))
								+ "]: DB entry not found.");
						continue; // to avoid nullpointer in case url does not
									// exists in the database.
					}
					rs.first();
					reposURLs.put(rs.getInt("id"), l.get(i - 1));
				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				}

			}

			break;
		case "db":
		default:
			reposURLs = new IOHandler().readURLsFromDatabase();
			break;
		}

		System.out.println("..........[Done].");
		return reposURLs;
	}

}
