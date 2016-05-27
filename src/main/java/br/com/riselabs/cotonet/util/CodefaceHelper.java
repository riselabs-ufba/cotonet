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

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;
import br.com.riselabs.cotonet.model.exceptions.InvalidNumberOfTagsException;

/**
 * @author Alcemir R. Santos
 *
 */
public class CodefaceHelper {

	private static IOHandler io = new IOHandler();

	/**
	 * Creates a shell script for start a codeface execution with all systems.
	 * 
	 * @param target_systems
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws EmptyContentException
	 */
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
		File sh = new File(Directories.getScriptsDir(), "run_target-systems.sh");
		io.checkAndRemove(sh);
		io.writeFile(sh, content);
		Logger.log("Codeface execution shell script written at: "
				+ sh.getCanonicalPath());
	}

	/**
	 * Writes the {@code .conf} file to be used with codeface.
	 * 
	 * @param log
	 * 
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws EmptyContentException
	 * @throws InvalidNumberOfTagsException
	 */
	public static void createCodefaceConfFiles(Project project)
			throws IOException, NullPointerException, EmptyContentException {
		createMergeBasedTags(project);
		String releases = null;
		List<String> tuples = new ArrayList<String>();
		for (MergeScenario ms : project.getMergeScenarios()) {
			if (ms.getID() == null) {
				continue;
			}
			tuples.add("\"" + project.getName() + "T" + ms.getID() + "\", \""
					+ project.getName() + "B" + ms.getID());
		}
		releases = createReleasesString(tuples);

		StringBuilder sb = new StringBuilder();
		sb.append("# Codeface configuration file for '" + project.getName()
				+ "'");
		sb.append(" \n");
		sb.append("# Copyright UFBA/UniPassau - Alcemir Santos\n");
		sb.append("#");
		sb.append("# Copying and distribution of this file, with or without modification,\n");
		sb.append("# are permitted in any medium without royalty provided the copyright\n");
		sb.append("# notice and this notice are preserved.  This file is offered as-is,\n");
		sb.append("# without any warranty.\n");
		sb.append("\n");
		sb.append("---\n");
		sb.append("project: " + project.getName() + "\n");
		sb.append("repo: " + project.getName());
		sb.append(" # Relative to git-dir as specified on the command line\n");
		sb.append("description: " + project.getName() + "\n");
		sb.append("mailinglists: \n");
		sb.append("    -   name: \n");
		sb.append("        type:\n");
		sb.append("        source:\n");
		sb.append("#    - {name: gmane.comp.emulators.qemu.user, type: user, source: gmane}\n");
		sb.append("revisions: [" + releases + " ]\n");
		sb.append("# rcs : [" + releases + " ]\n");
		sb.append("# tagging types: proximity, tag, file, feature, committer2author, feature_file\n");
		sb.append("\n" + "tagging: file\n" + "");

		List<String> content = new ArrayList<String>();
		content.add(sb.toString());

		File systemDir = new File(Directories.getConfigDir(), project.getName()
				+ ".conf");
		io.writeFile(systemDir, content);
		Logger.log(
				new File(Directories.getLogDir(), "thread-" + project.getName()
						+ ".log"), "[" + project.getName()
						+ "] Codeface configuration file written.");
	}

	private static void createMergeBasedTags(Project project)
			throws IOException {
		Map<String, String> tagsMap;
		Repository repository = project.getRepository();
		List<MergeScenario> scenarios = new ArrayList<MergeScenario>(
				project.getMergeScenarios());
		try (Git git = new Git(repository)) {
			// remove existing tags
			for (String tag : repository.getTags().keySet()) {
				git.tagDelete().setTags(tag).call();
			}
		} catch (GitAPIException e) {
			Logger.logStackTrace(e);
		}

		tagsMap = new HashMap<String, String>();
		for (MergeScenario s : scenarios) {
			if (s.getID() == null) {
				continue; // XXX handling ghosts exceptions
			}
			String tagT = project.getName() + "T" + s.getID();
			String tagB = project.getName() + "B" + s.getID();

			tagsMap.put(tagB, s.getBase().getName());
			RevCommit earlier = getEarlierCommit(project.getRepository(),
					s.getBase(), 3);
			tagsMap.put(tagT, earlier.getName());

			// prepare test-repository
			try (Git git = new Git(repository)) {

				try (RevWalk walk = new RevWalk(repository)) {

					git.tag().setObjectId(s.getBase()).setName(tagB).call();
					git.tag().setObjectId(earlier).setName(tagT).call();

					walk.dispose();
				} catch (ConcurrentRefUpdateException e) {
					e.printStackTrace();
				} catch (InvalidTagNameException e) {
					e.printStackTrace();
				} catch (NoHeadException e) {
					e.printStackTrace();
				} catch (GitAPIException e) {
					e.printStackTrace();
				}
			} catch (RevisionSyntaxException e) {
				Logger.logStackTrace(e);
			}
		}
		try {
			createTagToSHA1MappingFile(project.getName(), tagsMap);
		} catch (NullPointerException | EmptyContentException e) {
			Logger.logStackTrace(
					io.getDirectory(new File(Directories.getLogDir(), project
							.getName() + ".log")), e);
		}
	}

	/**
	 * Returns a commit from the given repository dated {@code i} months
	 * earlier.
	 * 
	 * @param repo
	 * @param baseCommit
	 * @param i
	 * @return
	 * @throws IOException
	 * @throws IncorrectObjectTypeException
	 * @throws MissingObjectException
	 */
	private static RevCommit getEarlierCommit(Repository repo,
			RevCommit baseCommit, int i) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		RevCommit result = null;
		try (RevWalk rw = new RevWalk(repo)) {
			RevCommit base = rw.parseCommit(baseCommit.getId());
			rw.markStart(base);
			Date baseDate = base.getAuthorIdent().getWhen();
			Instant instant = baseDate.toInstant();
			ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
			LocalDate reference = zdt.toLocalDate();
			RevCommit aux;
			while ((aux = rw.next()) != null) {
				Date resultDate = aux.getAuthorIdent().getWhen();
				instant = resultDate.toInstant();
				zdt = instant.atZone(ZoneId.systemDefault());
				LocalDate imonths = zdt.toLocalDate();
				long days = ChronoUnit.DAYS.between(imonths, reference);
				if ((days % 30) > i) {
					return aux;
				} else {
					// in case it reaches the beginning of the tree returns the
					// last commit
					result = aux;
				}
			}
		}
		return result;
	}

	/**
	 * Writes a file with mapping from each tag created to its SHA1.
	 * 
	 * @param tagsMap
	 * 
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws EmptyContentException
	 */
	private static void createTagToSHA1MappingFile(String projectName,
			Map<String, String> tagsMap) throws NullPointerException,
			IOException, EmptyContentException {
		List<String> lines = new ArrayList<String>();

		for (Entry<String, String> e : tagsMap.entrySet()) {
			lines.add(e.getKey() + ": " + e.getValue());
		}
		File mappingFile = new File(Directories.getMappingsDir(),
				projectName + "_TAGsMapping.txt");
		io.writeFile(mappingFile, lines);
		Logger.log(new File(Directories.getLogDir(), "thread-" + projectName
				+ ".log"), "[" + projectName + "] Tags mapping file written.");
	}

	private static String createReleasesString(List<String> tagsList) {
		if (tagsList.size() == 1) {
			return tagsList.remove(0);
		} else {
			return tagsList.remove(0) + ", " + createReleasesString(tagsList);
		}
	}

	/**
	 * Returns the list of TAGs from the given repository.
	 * 
	 * @param repository
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	@Deprecated
	public List<String> getTags(Repository repository) throws IOException,
			GitAPIException {
		List<String> tags = new ArrayList<String>();
		try (Git git = new Git(repository)) {
			List<Ref> call = git.tagList().call();
			for (Ref ref : call) {
				tags.add(ref.getName());
			}
		}
		return tags;
	}
}
