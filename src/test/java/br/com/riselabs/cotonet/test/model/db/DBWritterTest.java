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
package br.com.riselabs.cotonet.test.model.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.After;
import org.junit.Test;

import br.com.riselabs.cotonet.builder.NetworkBuilder;
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
import br.com.riselabs.cotonet.test.helpers.ConflictBasedRepositoryTestCase;

/**
 * @author Alcemir R. Santos
 *
 */
public class DBWritterTest extends ConflictBasedRepositoryTestCase {

	private NetworkBuilder builder;
	
	@After
	public void setup(){
	}
	
	@Test
	public void saveNetwork() throws Exception {
		setResolvedMergeConflictScenario();
		Project aProject = new Project("http://github.com/test", db);
		NetworkType programTypeTest = NetworkType.CHUNK_BASED;
		builder = new NetworkBuilder(aProject, programTypeTest);
		builder.build();
		builder.persist();
		
		// assert project saved
		ProjectDAO dao = (ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT);
		aProject = dao.get(aProject);
		assertTrue(aProject.getID()==1);
		
		// assert merge scenario saved
		MergeScenarioDAO msdao = (MergeScenarioDAO) DAOFactory
				.getDAO(CotonetBean.MERGE_SCENARIO);
		MergeScenario ms = msdao.list().remove(0);
		assertTrue(ms.getID()==1);
		assertTrue(ms.getProjectID()==aProject.getID());
		
		// assert conflict network saved
		ConflictBasedNetworkDAO cndao = (ConflictBasedNetworkDAO) DAOFactory.getDAO(CotonetBean.CONFLICT_NETWORK);
		ConflictBasedNetwork connet = cndao.list().remove(0);
		assertTrue(connet.getID()==1);
		assertTrue(connet.getMergeScenarioID()==ms.getID());
		assertEquals(NetworkType.CHUNK_BASED, connet.getType());
		
		// assert edges saved
		DeveloperEdgeDAO edao = (DeveloperEdgeDAO) DAOFactory.getDAO(CotonetBean.EDGE);
		Iterator<DeveloperEdge> iEdges = edao.list().iterator();
		DeveloperEdge edge = iEdges.next();
		assertTrue(edge.getDevA().getID()==1);
		assertTrue(edge.getDevB().getID()==2);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getDevA().getID()==1);
		assertTrue(edge.getDevB().getID()==3);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getDevA().getID()==1);
		assertTrue(edge.getDevB().getID()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getDevA().getID()==2);
		assertTrue(edge.getDevB().getID()==3);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getDevA().getID()==2);
		assertTrue(edge.getDevB().getID()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getDevA().getID()==3);
		assertTrue(edge.getDevB().getID()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getDevA().getID()==2);
		assertTrue(edge.getDevB().getID()==5);
		assertTrue(edge.getNetworkID()==connet.getID());
		assertFalse(iEdges.hasNext());
		
		// assert developers saved
		DeveloperNodeDAO ddao = (DeveloperNodeDAO) DAOFactory.getDAO(CotonetBean.NODE);
		Iterator<DeveloperNode> iNodes = ddao.list().iterator();
		DeveloperNode node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devb@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devc@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("deva@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("deve@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devd@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		assertFalse(iNodes.hasNext());
	}
	
}
