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
package br.com.riselabs.cotonet.test.builder.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.blame.BlameResult;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.builder.commands.RecursiveBlame;
import br.com.riselabs.cotonet.model.beans.Blame;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.test.helpers.ConflictBasedRepositoryTestCase;

/**
 * 
 * @author Alcemir R. Santos
 *
 */
public class RecursiveBlameTest extends ConflictBasedRepositoryTestCase {

	RecursiveBlame blamer;
	
	@Before
	public void setup() {
		blamer =  new RecursiveBlame();
	}

	@Test
	public void shouldRetriveBlamesFromTheRightBranch() throws Exception {
		MergeScenario scenario = setCollaborationScenarioInTempRepository();

		List<Blame<BlameResult>> blames = blamer.setRepository(db)
				.setBeginRevision(scenario.getRight())
				.setEndRevision(scenario.getBase()).setFilePath("Foo.java")
				.call();
		
		assertTrue(blames.size()==3);
		Iterator<Blame<BlameResult>> i = blames.iterator();
		Blame<BlameResult> b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("s3"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("s2"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("s1"));
		assertFalse(i.hasNext());
	}
	
	@Test
	public void shouldRetriveBlamesFromTheLeftBranch() throws Exception {
		MergeScenario scenario = setCollaborationScenarioInTempRepository();

		List<Blame<BlameResult>> blames = blamer.setRepository(db)
				.setBeginRevision(scenario.getLeft())
				.setEndRevision(scenario.getBase()).setFilePath("Foo.java")
				.call();
		
		assertTrue(blames.size()==3);
		Iterator<Blame<BlameResult>> i = blames.iterator();
		Blame<BlameResult> b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("m3"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("m2"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("m1"));
		assertFalse(i.hasNext());
	}
	
	
	@Test
	public void shouldRetriveBlamesFromBothBranch() throws Exception {
		MergeScenario scenario = setCollaborationScenarioInTempRepository();

		List<Blame<BlameResult>> blames = blamer.setRepository(db)
				.setBeginRevision(scenario.getRight())
				.setEndRevision(scenario.getBase()).setFilePath("Foo.java")
				.call();
		blames.addAll(blamer.setRepository(db)
				.setBeginRevision(scenario.getLeft())
				.setEndRevision(scenario.getBase()).setFilePath("Foo.java")
				.call());
		
		assertTrue(blames.size()==6);
		Iterator<Blame<BlameResult>> i = blames.iterator();
		Blame<BlameResult> b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("s3"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("s2"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("s1"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("m3"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("m2"));
		b = i.next();
		assertTrue(b.getRevision().getFullMessage().equals("m1"));
		assertFalse(i.hasNext());
	}
}
