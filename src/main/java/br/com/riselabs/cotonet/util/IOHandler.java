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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.db.DBManager;
import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;

public class IOHandler {

	public List<String> readFile(File fin) {
		List<String> lines = null;
		try {
			FileInputStream fis = new FileInputStream(fin);

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
		writer = new BufferedWriter(new FileWriter(file, true));
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

		for (String line : content) {
			appendLineToFile(file, line);
		}
	}

	/**
	 * Checks whether the "repos" directory correspondent to the given name
	 * already exists. Returns <code>null</code> if not.
	 * 
	 * @param folderName
	 * @return - the directory
	 * @throws IOException
	 */
	public File getReposDirectory(String folderName) throws IOException {
		File dir = new File(Directories.getReposDir() + folderName);
		return getDirectory(dir);
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
		File systemDir = new File(Directories.getReposDir() + sytemName);
		checkAndRemove(systemDir);
		makeDirectory(systemDir);
		System.gc();
		return systemDir;
	}

	/**
	 * rewrite the repo repository.
	 * 
	 * @param folderName
	 * @return
	 * @throws IOException
	 */
	public File makeDirectory(File aPath) throws IOException {
		aPath.mkdirs();
		return aPath;
	}

	/**
	 * Returns the URLs from the "ghanalysis" database.
	 * 
	 * @return - a map <id, url> for each entry in the "repository" table of the
	 *         database.
	 * @throws IOException 
	 */
	public Map<Integer, String> readURLsFromDatabase() throws IOException {
		Map<Integer, String> result = new HashMap<Integer, String>();
		try {
			ResultSet rs = DBManager
					.executeQuery("select r.id ids, r.url urls from repository as r");
			while (rs.next()) {
				Integer id = rs.getInt("ids");
				String url = rs.getString("urls");
				url = url.replace("http:", "https:");
				result.put(id, url);
			}
		} catch (ClassNotFoundException e) {
			// getConnection fail.
			e.printStackTrace();
		} catch (SQLException e) {
			// prepareStatement fail.
			e.printStackTrace();
		}
		return result;
	}

	public static List<MergeScenario> getMergeScenarios(Integer repositoryID, Repository repo) throws IOException {
		List<MergeScenario> scenarios = new ArrayList<MergeScenario>();
		String sql = "select  " + "m.leftrevision \"left\", "
				+ "m.baserevision \"base\", " + "m.rightrevision \"right\", "
				+ "m.leftcommits \"nlcommits\","
				+ "m.rightcommits \"nrcommits\"," + "l.commitid \"leftsha\","
				+ "b.commitid \"basesha\"," + "r.commitid \"rightsha\" "
				+ "from mergescenario m, revision l, revision b, revision r "
				+ "where m.leftrepo = " + repositoryID + " and m.rightrepo = "
				+ repositoryID + " and m.diffstatsdone "
				+ "and m.gitstatsdone " + "and m.commitstatsdone "
				+ "and m.leftrevision=l.id " + "and m.baserevision=b.id "
				+ "and m.rightrevision=r.id " + "and m.leftcommits > 0 "
				+ "and m.rightcommits > 0";
		// + " limit ", max_scenarios, sep="";

		try {
			ResultSet rs = DBManager.executeQuery(sql);
			while (rs.next()) {
				try(RevWalk w = new RevWalk(repo)){
					RevCommit left = w.parseCommit(repo.resolve(rs.getString("leftsha")));
					RevCommit base = w.parseCommit(repo.resolve(rs.getString("basesha")));
					RevCommit right = w.parseCommit(repo.resolve(rs.getString("rightsha")));
					scenarios.add(new MergeScenario(repositoryID, base, left, right));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return scenarios;
	}

	public void createCodefaceRunScript(List<String> target_systems)
			throws IOException, NullPointerException, EmptyContentException {
		String str = "#!/bin/sh\n";
		for (String s : target_systems) {
			str += "echo \"============================\"\n"
					+ "echo \"[Running " + s + "]: Start\"\n"
					+ "codeface run -p conf/" + s + ".conf results/ repos/\n"
					+ "echo \"[Running " + s + "]: End\"\n" + "echo \n";
		}
		str += "echo \"[Running Target Systems]: Done.\"";

		List<String> content = new ArrayList<String>();
		content.add(str);
		File sh = new File(Directories.getConfigDir(), "run_target-systems.sh");
		checkAndRemove(sh);
		writeFile(sh, content);
		Logger.log("Codeface execution shell script written at: "
				+ sh.getCanonicalPath());
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