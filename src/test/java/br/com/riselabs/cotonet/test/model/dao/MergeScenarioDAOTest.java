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
package br.com.riselabs.cotonet.test.model.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import org.eclipse.jgit.junit.RepositoryTestCase;
import org.eclipse.jgit.junit.TestRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.MergeScenarioDAO;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.test.helpers.DBTestCase;

/**
 * @author Alcemir R. Santos
 *
 */
public class MergeScenarioDAOTest extends RepositoryTestCase  {

	private MergeScenarioDAO dao;

	private TestRepository<Repository> util;
	
	@Before
	public void setup() throws Exception {
		super.setUp();
		util = new TestRepository<Repository>(db, new RevWalk(db));
		
		DBTestCase.resetTestDB();
		dao = (MergeScenarioDAO) DAOFactory.getDAO(CotonetBean.MERGE_SCENARIO);
		Project p = new Project("http://github.com/test", null);
		((ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT)).save(p);
	}

	@After
	public void teardown() throws ClassNotFoundException, URISyntaxException,
			SQLException, IOException {
		DBTestCase.resetTestDB();
	}

	@Test(expected = IllegalArgumentException.class)
	public void saveEmptyMergeScenario() {
		MergeScenario ms = new MergeScenario();
		assertNull(ms.getID());
		assertNull(ms.getProjectID());
		assertNull(ms.getBase());
		assertNull(ms.getLeft());
		assertNull(ms.getRight());
		assertNull(ms.getSHA1Base());
		assertNull(ms.getSHA1Left());
		assertNull(ms.getSHA1Right());
		dao.save(ms);
	}

	@Test(expected = IllegalArgumentException.class)
	public void saveMergeScenarioWithoutSystemID() throws Exception {
		
		MergeScenario ms = new MergeScenario(null, commit(), commit(), commit());
		assertNull(ms.getID());
		assertNull(ms.getProjectID());
		assertNotNull(ms.getBase());
		assertNotNull(ms.getLeft());
		assertNotNull(ms.getRight());
		assertNull(ms.getSHA1Base());
		assertNull(ms.getSHA1Left());
		assertNull(ms.getSHA1Right());
		dao.save(ms);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveMergeScenarioWithoutBaseCommit() throws Exception {
		MergeScenario ms = new MergeScenario(1, null, commit(), commit());
		assertNull(ms.getID());
		assertTrue(ms.getProjectID()==1);
		assertNull(ms.getBase());
		assertNotNull(ms.getLeft());
		assertNotNull(ms.getRight());
		assertNull(ms.getSHA1Base());
		assertNull(ms.getSHA1Left());
		assertNull(ms.getSHA1Right());
		assertTrue(dao.save(ms));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveMergeScenarioWithoutLeftCommit() throws Exception {
		MergeScenario ms = new MergeScenario(1, commit(), null, commit());
		assertNull(ms.getID());
		assertTrue(ms.getProjectID()==1);
		assertNotNull(ms.getBase());
		assertNull(ms.getLeft());
		assertNotNull(ms.getRight());
		assertNull(ms.getSHA1Base());
		assertNull(ms.getSHA1Left());
		assertNull(ms.getSHA1Right());
		assertTrue(dao.save(ms));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveMergeScenarioWithoutRightCommit() throws Exception {
		MergeScenario ms = new MergeScenario(1, commit(), commit(), null);
		assertNull(ms.getID());
		assertTrue(ms.getProjectID()==1);
		assertNotNull(ms.getBase());
		assertNotNull(ms.getLeft());
		assertNull(ms.getRight());
		assertNull(ms.getSHA1Base());
		assertNull(ms.getSHA1Left());
		assertNull(ms.getSHA1Right());
		assertTrue(dao.save(ms));
	}
	
	@Test
	public void saveMergeScenarioSuccessfully() throws Exception {
		MergeScenario ms = new MergeScenario(1, commit(), commit(), commit());
		assertNull(ms.getID());
		assertTrue(ms.getProjectID()==1);
		assertNotNull(ms.getBase());
		assertNotNull(ms.getLeft());
		assertNotNull(ms.getRight());
		assertNull(ms.getSHA1Base());
		assertNull(ms.getSHA1Left());
		assertNull(ms.getSHA1Right());
		assertTrue(dao.save(ms));
	}
	
	@Test
	public void getMergeScenarioByCommitTuple() throws Exception {
		RevCommit base = commit();
		RevCommit left =  commit();
		RevCommit right = commit();
		MergeScenario ms = new MergeScenario(1, base, left, right);
		assertTrue(dao.save(ms));
		
		MergeScenario retrieved = dao.get(new MergeScenario(null, base, left, right));
		assertTrue(retrieved.getID()==1);
		assertEquals(base.name(), retrieved.getSHA1Base());
		assertEquals(left.name(), retrieved.getSHA1Left());
		assertEquals(right.name(), retrieved.getSHA1Right());
	}
	
	@Test
	public void getMergeScenarioByID() throws Exception {
		RevCommit base = commit();
		RevCommit left =  commit();
		RevCommit right = commit();
		MergeScenario ms = new MergeScenario(1, base, left, right);
		assertTrue(dao.save(ms));
		
		MergeScenario retrieved = dao.get(new MergeScenario(1, null, null, null, null));
		assertTrue(retrieved.getID()==1);
		assertEquals(base.name(), retrieved.getSHA1Base());
		assertEquals(left.name(), retrieved.getSHA1Left());
		assertEquals(right.name(), retrieved.getSHA1Right());
	}
	
	protected RevCommit commit(final RevCommit... parents) throws Exception {
		return util.commit(parents);
	}
}
