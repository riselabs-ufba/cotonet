package br.com.riselabs.connet.test.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.junit.RepositoryTestCase;
import org.eclipse.jgit.junit.TestRepository;
import org.eclipse.jgit.junit.TestRepository.BranchBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import br.com.riselabs.connet.beans.JGitMergeScenario;

public class MyOwnHelper extends RepositoryTestCase {

	public MyOwnHelper(){
	}
	

	public static void printMergeResult(MergeResult result) {
		Map<String, int[][]> allConflicts = result.getConflicts();
		for (String path : allConflicts.keySet()) {
			int[][] c = allConflicts.get(path);
			System.out.println("Conflicts in file " + path);
			for (int i = 0; i < c.length; ++i) {
				System.out.println("  Conflict #" + i);
				for (int j = 0; j < (c[i].length) - 1; ++j) {
					if (c[i][j] >= 0)
						System.out.println("    Chunk for "
								+ result.getMergedCommits()[j]
								+ " starts on line #" + c[i][j]);
				}
			}
		}
	}

	public static void log(Repository repo) throws Exception {
		Git git = Git.wrap(repo);
		Iterable<RevCommit> commits = git.log().all().call();
		for (RevCommit c : commits) {
			System.out.println("time(s): " + c.getCommitTime() + ", author: "
					+ c.getAuthorIdent().getName());
		}
	}

	public static void logFileChunkEvolution(Repository repo, File file,
			int beginLine, int endLine) {
		Git git = Git.wrap(repo);
		// TODO create log of the evolution of a given file
		// Iterable<RevCommit> logs =
		// git.log().addPath(file.getPath().toString()).addRange(beginLine,
		// endLine);
	}

	public static void showBlame(Repository db, String file) throws Exception {
		BlameCommand blamer = new BlameCommand(db);
		ObjectId commitID = db.resolve("HEAD");
		blamer.setStartCommit(commitID);
		blamer.setFilePath(file);
		BlameResult blame = blamer.call();

		// read the number of lines from the commit to not look at changes
		// in the working copy
		int lines = countFiles(db, commitID, file);
		for (int i = 0; i < lines; i++) {
			RevCommit commit = blame.getSourceCommit(i);
			System.out.println("Line: " + i + ": " + commit);
		}

		System.out.println("Displayed commits responsible for " + lines
				+ " lines of " + file);
	}

	public static int countFiles(Repository repository, ObjectId commitID,
			String name) throws IOException {
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevCommit commit = revWalk.parseCommit(commitID);
			RevTree tree = commit.getTree();
			System.out.println("Having tree: " + tree);

			// now try to find a specific file
			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				treeWalk.setFilter(PathFilter.create(name));
				if (!treeWalk.next()) {
					throw new IllegalStateException(
							"Did not find expected file 'README.md'");
				}

				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = repository.open(objectId);

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// and then one can the loader to read the file
				loader.copyTo(stream);

				revWalk.dispose();

				return IOUtils.readLines(
						new ByteArrayInputStream(stream.toByteArray())).size();
			}
		}
	}

	public static void walkAllCommits(Git git) throws Exception {
		Iterable<RevCommit> commits = git.log().all().call();
		int count = 0;
		for (RevCommit commit : commits) {
			System.out.println("LogCommit: " + commit);
			count++;
		}
		System.out.println(count);
	}
	public FileRepository getdb() {
		return db;
	}
	
	/**
	 * Creates a collaboration scenario with five developers (Devs A, B, C, D,
	 * and E) and two files (Foo.java and Bar.java).
	 * 
	 * @return
	 * @throws Exception
	 */
	public JGitMergeScenario setCollaborationScenario(boolean shouldWriteFS)
			throws Exception {
		Git git = Git.wrap(db);
		PersonIdent devY = new PersonIdent("Dev Y", "devy@project.com");
		PersonIdent devX = new PersonIdent("Dev X", "devx@project.com");
		PersonIdent devA = new PersonIdent("Dev A", "deva@project.com");
		PersonIdent devB = new PersonIdent("Dev B", "devb@project.com");
		PersonIdent devC = new PersonIdent("Dev C", "devc@project.com");
		PersonIdent devD = new PersonIdent("Dev D", "devd@project.com");
		PersonIdent devE = new PersonIdent("Dev E", "deve@project.com");

		RevCommit mergeBaseCommit, lastMasterCommit, lastSideCommit;

		// checks whether it should write in the filesystem or not.
		if (shouldWriteFS) {
			// first versions of Foo and Bar
			writeTrashFile("Foo.java", "1");
			writeTrashFile("Bar.java", "1");
			git.add().addFilepattern("Foo.java").addFilepattern("Bar.java")
					.call();
			git.commit().setMessage("initial commit").setAuthor(devY)
					.call();
			
			writeTrashFile("Foo.java", "1\n2\n3\n4\n5\n6\n7\n8\n");
			writeTrashFile("Bar.java", "1\n2\n3\n4\n");
			git.add().addFilepattern("Foo.java").addFilepattern("Bar.java")
					.call();
			mergeBaseCommit = git.commit().setMessage("m0").setAuthor(devX)
					.call();

			// Dev E changes Foo
			writeTrashFile("Foo.java", "1\n2\n3\n4\n5-master\n6\n7\n8\n");
			git.add().addFilepattern("Foo.java").call();
			git.commit().setMessage("m1").setAuthor(devE).call();

			// Dev C changes Foo
			writeTrashFile("Foo.java", "1\n2\n3\n4-master\n5\n6\n7\n8\n");
			writeTrashFile("Bar.java", "1\n2\n3-master\n4-master\n");
			git.add().addFilepattern("Foo.java").addFilepattern("Bar.java")
					.call();
			git.commit().setMessage("m2").setAuthor(devC).call();

			// Dev B changes
			writeTrashFile("Bar.java", "1\n2\n3\n4-master\n");
			git.add().addFilepattern("Bar.java").call();
			lastMasterCommit = git.commit().setMessage("m3").setAuthor(devB)
					.call();

			// updating the tree with the changes
			createBranch(mergeBaseCommit, "refs/heads/side");
			checkoutBranch("refs/heads/side");

			// Dev D changes Foo
			writeTrashFile("Foo.java", "1\n2\n3\n4-side\n5\n6\n7\n8\n");
			git.add().addFilepattern("Foo.java").call();
			git.commit().setMessage("s1").setAuthor(devD).call();

			// Dev E changes Bar
			writeTrashFile("Bar.java", "1\n2\n3\n4\n5\n");
			git.add().addFilepattern("Bar.java").call();
			git.commit().setMessage("s2").setAuthor(devE).call();

			// Dev A changes Bar
			writeTrashFile("Bar.java", "1\n2\n3-side\n4-side\n5\n");
			git.add().addFilepattern("Bar.java").call();
			lastSideCommit = git.commit().setMessage("s3").setAuthor(devA)
					.call();

		} else {

			TestRepository<Repository> db_t = new TestRepository<Repository>(db);

			BranchBuilder master = db_t.branch("master");

			// first versions of Foo and Bar
			master.commit() 
					.add("Foo.java", "1")
					.add("Bar.java", "1")
					.message("initial commit").author(devY)
					.create();
			
			mergeBaseCommit = master.commit()
					.add("Foo.java", "1\n2\n3\n4\n5\n6\n7\n8\n")
					.add("Bar.java", "1\n2\n3\n4\n").message("m0").author(devX)
					.create();

			// Dev E changes Foo
			RevCommit masterCommit1 = master.commit()
					.add("Foo.java", "1\n2\n3\n4\n5-master\n6\n7\n8\n")
					.message("m1").author(devE).create();

			// Dev C changes Foo
			RevCommit masterCommit2 = master.commit()
					.add("Foo.java", "1\n2\n3\n4-master\n5\n6\n7\n8\n")
					.add("Bar.java", "1\n2\n3-master\n4-master\n")
					.message("m2").author(devC).create();

			// Dev B changes
			lastMasterCommit = master.commit()
					.add("Bar.java", "1\n2\n3\n4-master\n").message("m3")
					.author(devB).create();

			// updating the tree with the changes
			db_t.getRevWalk().parseCommit(mergeBaseCommit);

			// creating a new branc: side
			BranchBuilder side = db_t.branch("side");

			// Dev D changes Foo
			RevCommit sideCommit1 = side.commit().parent(mergeBaseCommit)
					.add("Foo.java", "1\n2\n3\n4-side\n5\n6\n7\n8\n")
					.message("s1").author(devD).create();

			// Dev E changes Bar
			RevCommit sideCommit2 = side.commit()
					.add("Bar.java", "1\n2\n3\n4\n5\n").message("s2")
					.author(devE).create();

			// Dev A changes Bar
			lastSideCommit = side.commit()
					.add("Bar.java", "1\n2\n3-side\n4-side\n5\n").message("s3")
					.author(devA).create();

			git.checkout().setName("master").setStartPoint(lastMasterCommit)
					.call();
		}
		return new JGitMergeScenario(mergeBaseCommit, lastMasterCommit,
				lastSideCommit);
	}
}
