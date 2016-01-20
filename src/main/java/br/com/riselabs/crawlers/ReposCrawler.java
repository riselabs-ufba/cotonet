package br.com.riselabs.crawlers;

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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

public class ReposCrawler {

	private static final String USER_HOME = System.getProperty("user.home");
	private static final String REPOS_DIR = USER_HOME
			+ "/Downloads/ReposCrawler/";
	private static ReposCrawler instance;
	
	private ReposCrawler() {
	}
	
	public static ReposCrawler getInstance() {
		if (instance == null)
			instance = new ReposCrawler();
		return instance;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		ReposCrawler crawler = ReposCrawler.getInstance();

		// read the target_systems_repo_urls 
		String filename = "target_systems_repo_urls.txt";
		File urlsFile = new File(REPOS_DIR + filename);		
		List reposURLs = ReaderUtil.readFile2(urlsFile.toPath());

		// for each url in the list clones the repository
		for (String repoURL : (List<String>) reposURLs) {
			Repository repository;
			try {

				repository = crawler.cloneRepository(repoURL);
				List tags = crawler.getTagsList(repository);
				crawler.writeFile(crawler.getPathName(repoURL), tags);
				// workaround for
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
				repository.close();

			} catch (InvalidRemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// TODO clonar repositorio


	public Repository cloneRepository(String remoteURL) throws IOException,
			InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		File localPath = makeDirectory(remoteURL);

		// then clone
		System.out.println("Cloning from " + remoteURL + " to " + localPath);
		try (Git result = Git.cloneRepository().setURI(remoteURL + ".git")
				.setDirectory(localPath).call()) {
			// Note: the call() returns an opened repository already which
			// needs
			// to be closed to avoid file handle leaks!
			System.out.println("Having repository: "
					+ result.getRepository().getDirectory());

			return result.getRepository();
		}
	}

	private File makeDirectory(String remoteURL) throws IOException {
		File localPath = new File(REPOS_DIR + getPathName(remoteURL));
		if (localPath.exists()) {
			System.out.println("Cleaning directory:" + localPath.toString());
			FileUtils.deleteDirectory(localPath);
		}
		localPath.mkdirs();
		System.gc();
		return localPath;
	}

	private String getPathName(String remoteURL) {
		String[] path = remoteURL.split("/");
		return path[path.length - 1];
	}

	// TODO salvar lista de tags num txt
	public List getTagsList(Repository repository) throws IOException,
			GitAPIException {
		List tags = new ArrayList<String>();
		try (Git git = new Git(repository)) {
			List<Ref> call = git.tagList().call();
			for (Ref ref : call) {
				tags.add(ref.getName());
			}
		}
		return tags;
	}

	public void writeFile(String targetSystemName, List tags) {
		BufferedWriter writer = null;
		try {
			// create a temporary file
			File logFile = new File(REPOS_DIR + targetSystemName + "_TAGs.txt");

			// This will output the full path where the file will be written
			// to...
			System.out.println(logFile.getCanonicalPath());

			writer = new BufferedWriter(new FileWriter(logFile));

			for (String tag : (List<String>) tags) {
				writer.write(tag + "\n");
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

	static class ReaderUtil {

		private static List readFile(File fin) {
			List urls = null;
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

		private static List readFile2(Path filePath) {
			Charset charset = Charset.forName("US-ASCII");
			List urls = null;
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
	}
}
