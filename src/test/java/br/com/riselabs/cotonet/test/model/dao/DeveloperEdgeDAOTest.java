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
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.ConflictBasedNetworkDAO;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.DeveloperEdgeDAO;
import br.com.riselabs.cotonet.model.dao.DeveloperNodeDAO;
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
public class DeveloperEdgeDAOTest extends ConflictBasedRepositoryTestCase{
	private DeveloperEdgeDAO dao;
	private TestRepository<Repository> util;
	
	@Before
	public void setup() throws Exception{
		super.setUp();
		util = new TestRepository<Repository>(db, new RevWalk(db));
		DBTestCase.resetTestDB();
		dao =  (DeveloperEdgeDAO) DAOFactory.getDAO(CotonetBean.EDGE);
		Project p =  new Project("http://github.com/test", null);
		((ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT)).save(p);
		DeveloperNode node = new DeveloperNode(1, "dev1", "dev1@project.com");
		((DeveloperNodeDAO) DAOFactory.getDAO(CotonetBean.NODE)).save(node);
		node = new DeveloperNode(1, "dev2", "dev2@project.com");
		((DeveloperNodeDAO) DAOFactory.getDAO(CotonetBean.NODE)).save(node);
		MergeScenario ms =  new MergeScenario(1, commit(), commit(), commit());
		((MergeScenarioDAO) DAOFactory.getDAO(CotonetBean.MERGE_SCENARIO)).save(ms);
		ConflictBasedNetwork conet = new ConflictBasedNetwork(1, 1, NetworkType.CHUNK_BASED);
		((ConflictBasedNetworkDAO) DAOFactory.getDAO(CotonetBean.CONFLICT_NETWORK)).save(conet);
	}
	
	@After
	public void teardown() throws ClassNotFoundException, URISyntaxException, SQLException, IOException{
		DBTestCase.resetTestDB();
	}
	
	@Test(expected=InvalidCotonetBeanException.class)
	public void saveEmptyEdge() throws InvalidCotonetBeanException{
		DeveloperEdge edge = new DeveloperEdge();
		assertNull(edge.getDevA());
		assertNull(edge.getDevB());
		assertNull(edge.getID());
		assertNull(edge.getNetworkID());
		dao.save(edge);
	}
	
	@Test(expected=InvalidCotonetBeanException.class)
	public void saveEdgeWithInexistentDevelopers() throws InvalidCotonetBeanException{
		DeveloperEdge edge = new DeveloperEdge(new DeveloperNode(34, null, null, null), new DeveloperNode(35, null, null, null));
		assertTrue(edge.getDevA().getID()==34);
		assertTrue(edge.getDevB().getID()==25);
		edge.setNetworkID(1);
		assertTrue(edge.getNetworkID()==1);
		dao.save(edge);
	}
	
	@Test(expected=InvalidCotonetBeanException.class)
	public void saveEdgeWithLeftInexistentDeveloper() throws InvalidCotonetBeanException{
		DeveloperEdge edge = new DeveloperEdge(new DeveloperNode(34, null, null, null), new DeveloperNode(2, null, null, null));
		assertTrue(edge.getDevA().getID()==34);
		assertTrue(edge.getDevB().getID()==2);
		edge.setNetworkID(1);
		assertTrue(edge.getNetworkID()==1);
		dao.save(edge);
	}
	
	@Test(expected=InvalidCotonetBeanException.class)
	public void saveEdgeWithRightInexistentDeveloper() throws InvalidCotonetBeanException{
		DeveloperEdge edge = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(35, null, null, null));
		assertTrue(edge.getDevA().getID()==1);
		assertTrue(edge.getDevB().getID()==25);
		edge.setNetworkID(1);
		assertTrue(edge.getNetworkID()==1);
		dao.save(edge);
	}
	
	@Test
	public void saveEdgeSuccessfully() throws InvalidCotonetBeanException{
		DeveloperEdge edge = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null));
		assertTrue(edge.getDevA().getID()==1);
		assertTrue(edge.getDevB().getID()==2);
		assertNull(edge.getID());
		edge.setNetworkID(1);
		assertTrue(edge.getNetworkID()==1);
		assertTrue(dao.save(edge));
	}
	
	@Test
	public void getEdgeWithNoID() throws InvalidCotonetBeanException{
		DeveloperEdge edge = new DeveloperEdge(null, 1, new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null),1);
		dao.save(edge);
		assertNull(edge.getID());
		edge =  dao.get(edge);
		assertTrue(edge.getID()==1);
	}
	
	@Test
	public void getEdgeByID() throws InvalidCotonetBeanException{
		DeveloperEdge edge = new DeveloperEdge(null, 1, new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null),1);
		dao.save(edge);
		assertNull(edge.getID());
		edge =  dao.get(new DeveloperEdge(1));
		assertTrue(edge.getDevA().getID()==1);
		assertTrue(edge.getDevB().getID()==2);
		assertTrue(edge.getNetworkID()==1);
	}
	
	protected RevCommit commit(final RevCommit... parents) throws Exception {
		return util.commit(parents);
	}
}
