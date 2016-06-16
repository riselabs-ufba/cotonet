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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import org.eclipse.jgit.junit.TestRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.ConflictBasedNetworkDAO;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.MergeScenarioDAO;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.test.helpers.ConflictBasedRepositoryTestCase;
import br.com.riselabs.cotonet.test.helpers.DBTestCase;

/**
 * @author Alcemir R. Santos
 *
 */
public class ConflictBasedNetworkDAOTest extends ConflictBasedRepositoryTestCase {

	private ConflictBasedNetworkDAO dao;
	
	private TestRepository<Repository> util;
	private MergeScenario ms;
	private Project p;

	private MergeScenarioDAO msdao;
	
	@Before
	public void setup() throws Exception{
		super.setUp();
		util = new TestRepository<Repository>(db, new RevWalk(db));
		
		DBTestCase.resetTestDB();
		dao =  (ConflictBasedNetworkDAO) DAOFactory.getDAO(CotonetBean.CONFLICT_NETWORK);
		p =  new Project("http://github.com/test", null);
		((ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT)).save(p);
		ms = new MergeScenario( 1, commit(), commit(), commit(), null, null);
		msdao = ((MergeScenarioDAO) DAOFactory.getDAO(CotonetBean.MERGE_SCENARIO));
		msdao.save(ms);
	}
	
	@After
	public void teardown() throws ClassNotFoundException, URISyntaxException, SQLException, IOException{
		DBTestCase.resetTestDB();
	}
	
	@Test(expected = InvalidCotonetBeanException.class)
	public void saveNetworkWithoutMergeScenarioID() throws InvalidCotonetBeanException{
		ConflictBasedNetwork conet = new ConflictBasedNetwork();
		assertEquals(NetworkType.CHUNK_BASED, conet.getType());
		assertNull(conet.getMergeScenarioID());
		dao.save(conet);
	}
	
	@Test
	public void saveNetworkSuccessfuly() throws InvalidCotonetBeanException{
		ConflictBasedNetwork conet = new ConflictBasedNetwork(p, ms);
		conet.setMergeScenarioID(msdao.get(ms).getID());
		assertEquals(NetworkType.CHUNK_BASED, conet.getType());
		assertTrue(conet.getMergeScenarioID()==1);
		assertTrue(dao.save(conet));
	}

	@Test
	public void getNetworkChunkBased() throws InvalidCotonetBeanException{
		ConflictBasedNetwork conet = new ConflictBasedNetwork(p, ms);
		conet.setMergeScenarioID(msdao.get(ms).getID());
		assertEquals(NetworkType.CHUNK_BASED, conet.getType());
		assertTrue(conet.getMergeScenarioID()==1);
		dao.save(conet);
		
		assertNull(conet.getID());
		conet = dao.get(conet);
		assertTrue(conet.getID()==1);
		assertTrue(conet.getMergeScenarioID()==1);
		assertEquals(NetworkType.CHUNK_BASED, conet.getType());
	}
	
	@Test
	public void getNetworkFileBased() throws InvalidCotonetBeanException{
		ConflictBasedNetwork conet = new ConflictBasedNetwork(p, ms);
		conet.setMergeScenarioID(msdao.get(ms).getID());
		conet.setType(NetworkType.FILE_BASED);
		assertEquals(NetworkType.FILE_BASED, conet.getType());
		assertTrue(conet.getMergeScenarioID()==1);
		dao.save(conet);
		
		assertNull(conet.getID());
		conet = dao.get(conet);
		assertTrue(conet.getID()==1);
		assertTrue(conet.getMergeScenarioID()==1);
		assertEquals(NetworkType.FILE_BASED, conet.getType());
	}
	
	@Test
	public void getNetworkFileBasedWithNullScenario() throws InvalidCotonetBeanException{
		ConflictBasedNetwork conet = new ConflictBasedNetwork(p, ms);
		conet.setMergeScenarioID(msdao.get(ms).getID());
		conet.setType(NetworkType.FILE_BASED);
		assertEquals(NetworkType.FILE_BASED, conet.getType());
		assertTrue(conet.getMergeScenarioID()==1);
		dao.save(conet);
		
		conet.setID(1);
		conet.setMergeScenarioID(null);
		conet = dao.get(conet);
		assertTrue(conet.getID()==1);
		assertTrue(conet.getMergeScenarioID()==1);
		assertEquals(NetworkType.FILE_BASED, conet.getType());
	}
	
	@Test
	public void getNetworkChunkBasedWithNullScenario() throws InvalidCotonetBeanException{
		ConflictBasedNetwork conet = new ConflictBasedNetwork(p, ms);
		conet.setMergeScenarioID(msdao.get(ms).getID());
		assertEquals(NetworkType.CHUNK_BASED, conet.getType());
		assertTrue(conet.getMergeScenarioID()==1);
		dao.save(conet);
		
		conet.setID(1);
		conet.setMergeScenarioID(null);
		conet = dao.get(conet);
		assertTrue(conet.getID()==1);
		assertTrue(conet.getMergeScenarioID()==1);
		assertEquals(NetworkType.CHUNK_BASED, conet.getType());
	}
	
	protected RevCommit commit(final RevCommit... parents) throws Exception {
		return util.commit(parents);
	}
}
