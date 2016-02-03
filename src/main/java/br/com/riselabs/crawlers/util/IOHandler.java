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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import br.com.riselabs.crawlers.beans.MergeScenario;
import br.com.riselabs.crawlers.db.DBManager;
import br.com.riselabs.crawlers.util.exceptions.EmptyContentException;
import br.com.riselabs.crawlers.util.exceptions.InvalidNumberOfTagsException;

public class IOHandler {

	public static List<String> readFile(File fin) {
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

	public static List<String> readFile(Path filePath) {
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

	public static void writeFile(File file, List<String> content)
			throws IOException, NullPointerException, EmptyContentException {
		if (content == null)
			throw new NullPointerException();
		if (content.isEmpty())
			throw new EmptyContentException();

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));

			for (String line : content) {
				writer.write(line + "\n");
			}

		} finally {
			// Close the writer regardless of what happens...
			writer.close();
		}
	}

	/**
	 * return the repo directory if it already exists
	 * 
	 * @param folderName
	 * @return
	 * @throws IOException
	 */
	public static File getDirectory(String folderName) throws IOException {
		File dir = new File(RCProperties.REPOS_DIR + folderName);
		if (dir.exists()) {
			return dir;
		}
		return null;
	}

	/**
	 * rewrite the repo repository.
	 * 
	 * @param folderName
	 * @return
	 * @throws IOException
	 */
	public static File makeDirectory(String folderName) throws IOException {
		File localPath = new File(RCProperties.REPOS_DIR + folderName);
		if (getDirectory(folderName) != null) {
			System.out.println("Cleaning directory: " + localPath.toString());
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

	public static Map<Integer, String> readURLsFromDatabase() {
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

	public static List<MergeScenario> getMergeScenarios(Integer repositoryID) {
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
				scenarios.add(new MergeScenario(rs.getString("basesha"), rs
						.getString("leftsha"), rs.getString("rightsha"), rs
						.getInt("nlcommits"), rs.getInt("nrcommits")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return scenarios;
	}

	public static void createCodefaceRunScript(List<String> target_systems)
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
		writeFile(
				new File(RCProperties.CODEFACE_DIR + "run_target-systems.sh"),
				content);
	}

	public static void createCodefaceConfFiles(String system, Integer numTags)
			throws IOException, NullPointerException, EmptyContentException, InvalidNumberOfTagsException {
		String releases = getTupletsString(numTags);

		String s = "# Configuration file for the system "
				+ system
				+ " \n"
				+ "# Copyright UFBA/UniPassau - Alcemir Santos\n"
				+ "#"
				+ "# Copying and distribution of this file, with or without modification,\n"
				+ "# are permitted in any medium without royalty provided the copyright\n"
				+ "# notice and this notice are preserved.  This file is offered as-is,\n"
				+ "# without any warranty.\n"
				+ "\n"
				+ "---\n"
				+ "project: "
				+ system
				+ "\n"
				+ "repo: "
				+ system
				+ " # Relative to git-dir as specified on the command line\n"
				+ "description: "
				+ system
				+ "\n"
				+ "mailinglists: \n"
				+ "    -   name: \n"
				+ "\n"
				+ "        type:\n"
				+ "\n"
				+ "        source:\n"
				+ "\n"
				+ "#    - {name: gmane.comp.emulators.qemu.user, type: user, source: gmane}\n"
				+ "\n"
				+ "revisions: ["
				+ releases
				+ " ]\n"
				+ "\n"
				// + "rcs : ["+ releases +" ]\n"
				+ "# tagging types: proximity, tag, file, feature, committer2author, feature_file\n"
				+ "\n" + "tagging: file\n" + "";

		List<String> content = new ArrayList<String>();
		content.add(s);
		writeFile(new File(RCProperties.CONFIG_DIR + system + ".conf"), content);
	}

	public static String getTupletsString(Integer numTags) throws InvalidNumberOfTagsException {
		Integer numScenarios;

		if ((numTags % 3) == 0 && numTags != 0 )
			numScenarios = numTags / 3;
		else
			throw new InvalidNumberOfTagsException();
		
		List<String> tuples = new ArrayList<String>();
		for (int i = 1; i <= numScenarios; i++) {
			tuples.add("\"B" + i + "\", \"L" + i + "\", \"R" + i + "\"");
		}
		return createReleasesString(tuples);
	}

	private static String createReleasesString(List<String> tagsList) {
		if (tagsList.size() == 1) {
			return tagsList.remove(0);
		} else {
			return tagsList.remove(0) + ", " + createReleasesString(tagsList);
		}
	}

	public static File getFile(File f) throws IOException {
		return getDirectory(f.getName());
	}

}