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
package br.com.riselabs.cotonet.test.helpers;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.merge.MergeStrategy;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.model.beans.MergeScenario;

/**
 * @author Alcemir R. Santos
 *
 */
public class RepositoryTestCaseTest extends ConflictBasedRepositoryTestCase {

	private Git git;
	
	@Before
	public void setup(){
		git = Git.wrap(db);
	}
	
	@Test
	public void mergeConflictScenarioInMemoryRepository() throws Exception {
		MergeScenario ms = setCollaborationScenarioInBareRepository();

		MergeResult result = git.merge().setStrategy(MergeStrategy.RECURSIVE)
				.include("side", ms.getRight()).call();
		assertEquals(MergeStatus.CONFLICTING, result.getMergeStatus());
	}
	
	@Test
	public void mergeConflictScenarioIsSettedInTempRepository()
			throws Exception {
		MergeScenario ms = setCollaborationScenarioInTempRepository();

		// asserting that files are different in both branches
		assertEquals("1\n2\n3\n4-side\n5\n6\n7\n8\n",
				read(new File(db.getWorkTree(), "Foo.java")));
		assertEquals("1\n2\n3-side\n4-side\n5\n",
				read(new File(db.getWorkTree(), "Bar.java")));
		checkoutBranch("refs/heads/master");
		assertEquals("1\n2\n3\n4-master\n5\n6\n7\n8\n",
				read(new File(db.getWorkTree(), "Foo.java")));
		assertEquals("1\n2\n3\n4-master\n", read(new File(db.getWorkTree(),
				"Bar.java")));

		// merging m3 with s3
		MergeResult result = git.merge().include(ms.getRight().getId()).call();
		assertEquals(MergeStatus.CONFLICTING, result.getMergeStatus());
	}
}
