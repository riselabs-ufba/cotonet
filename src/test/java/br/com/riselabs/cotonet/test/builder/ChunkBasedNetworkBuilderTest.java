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
package br.com.riselabs.cotonet.test.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.builder.ConflictBasedNetworkBuilder;
import br.com.riselabs.cotonet.builder.commands.GitConflictBlame;
import br.com.riselabs.cotonet.builder.commands.RecursiveBlame;
import br.com.riselabs.cotonet.model.beans.Blame;
import br.com.riselabs.cotonet.model.beans.ChunkBlame;
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
import br.com.riselabs.cotonet.test.helpers.DBTestCase;

/**
 * 
 * @author Alcemir R. Santos
 *
 */
public class ConflictBasedNetworkBuilderTest extends
		ConflictBasedRepositoryTestCase {

	private Git git;
	private ConflictBasedNetworkBuilder builder;

	@Before
	public void setup() throws Exception {
		super.setUp();
		DBTestCase.resetTestDB();
		git = Git.wrap(db);
		builder = new ConflictBasedNetworkBuilder();
	}
	
	@After
	public void teardown() throws Exception{
	}

	@Test
	public void buildFileBasedNetwork() throws Exception {
		MergeScenario aScenario = setCollaborationScenarioInTempRepository();
		Project aProject = new Project("", db);
		builder.setProject(aProject);
		
		MergeResult m = runMerge(aScenario);
		List<File> files = builder.getFilesWithConflicts(m);
		ConflictBasedNetwork connet = builder.build(aScenario, files, NetworkType.FILE_BASED);
		
		Iterator<DeveloperNode> iNodes = connet.getNodes().iterator();
		DeveloperNode node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("deva@project.com")));
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("deve@project.com")));
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devb@project.com")));
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devc@project.com")));
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devd@project.com")));
		assertFalse(iNodes.hasNext());
		
		Iterator<DeveloperEdge> iEdges = connet.getEdges().iterator();
		DeveloperEdge edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(1, 2)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(1, 3)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(1, 4)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(2, 3)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(2, 4)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(3, 4)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(5, 4)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(5, 2)));
		assertFalse(iEdges.hasNext());
	}

	@Test
	public void buildChunckBasedNetwork() throws Exception {
		MergeScenario aScenario = setCollaborationScenarioInTempRepository();
		Project aProject = new Project("", db);
		builder.setProject(aProject);
		
		MergeResult m = runMerge(aScenario);
		List<File> files = builder.getFilesWithConflicts(m);
		ConflictBasedNetwork connet = builder.build(aScenario, files, NetworkType.CHUNK_BASED);
		
		Iterator<DeveloperNode> iNodes = connet.getNodes().iterator();
		DeveloperNode node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devb@project.com")));
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devc@project.com")));
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("deva@project.com")));
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("deve@project.com")));
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devd@project.com")));
		assertFalse(iNodes.hasNext());
		
		Iterator<DeveloperEdge> iEdges = connet.getEdges().iterator();
		DeveloperEdge edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(1, 2)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(1, 3)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(1, 4)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(2, 3)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(2, 4)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(3, 4)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(2, 5)));
		assertFalse(iEdges.hasNext());
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

	@Test
	public void mergeConflictScenarioInMemoryRepository() throws Exception {
		MergeScenario ms = setCollaborationScenarioInBareRepository();

		MergeResult result = git.merge().setStrategy(MergeStrategy.RECURSIVE)
				.include("side", ms.getRight()).call();
		assertEquals(MergeStatus.CONFLICTING, result.getMergeStatus());
	}

	@Test
	public void shouldRetriveFooFileBasedContributors() throws Exception {
		builder.setProject(new Project("", db));
		MergeScenario scenario = setCollaborationScenarioInTempRepository();
		RecursiveBlame blame = new RecursiveBlame();

		List<Blame> blames = blame.setRepository(db)
				.setBeginRevision(scenario.getLeft())
				.setEndRevision(scenario.getBase()).setFilePath("Foo.java")
				.call();
		blames.addAll(blame.setRepository(db)
				.setBeginRevision(scenario.getRight())
				.setEndRevision(scenario.getBase()).setFilePath("Foo.java")
				.call());
		List<RevCommit> commits = builder.getCommitsFrom(scenario);
		List<DeveloperNode> list = builder.getDeveloperNodes(blames, commits);

		Iterator<DeveloperNode> i = list.iterator();
		DeveloperNode aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("Dev C", "devc@project.com")));
		assertTrue(aNode.getName().equals("Dev C"));
		assertTrue(aNode.getEmail().equals("devc@project.com"));
		aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("Dev E", "deve@project.com")));
		assertTrue(aNode.getName().equals("Dev E"));
		assertTrue(aNode.getEmail().equals("deve@project.com"));
		aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("Dev D", "devd@project.com")));
		assertTrue(aNode.getName().equals("Dev D"));
		assertTrue(aNode.getEmail().equals("devd@project.com"));
		assertFalse(i.hasNext());
	}

	@Test
	public void shouldRetriveBarChunkBasedContributors() throws Exception {
		builder.setProject(new Project("", db));
		MergeScenario scenario = setCollaborationScenarioInTempRepository();
		runMerge(scenario);
		String mergedfilepath = db.getDirectory().getParent().concat(File.separator+"Bar.java");
		List<ChunkBlame> blames =  GitConflictBlame.getConflictingLinesBlames(new File(mergedfilepath));
		
		List<DeveloperNode> list = builder.getDeveloperNodes(blames);

		Iterator<DeveloperNode> i = list.iterator();
		DeveloperNode aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("devb@project.com")));
		aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("devc@project.com")));
		aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("deva@project.com")));
		aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("deve@project.com")));
		assertFalse(i.hasNext());
	}
	
	
	@Test
	public void saveChunckBasedNetwork() throws Exception {
		setResolvedMergeConflictScenario();
		Project aProject = new Project("http://github.com/test", db);
		ConflictBasedNetworkBuilder builder = new ConflictBasedNetworkBuilder();
		builder.setProject(aProject);
		builder.execute();
		
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
		assertTrue(edge.getLeft()==1);
		assertTrue(edge.getRight()==2);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==1);
		assertTrue(edge.getRight()==3);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==1);
		assertTrue(edge.getRight()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==2);
		assertTrue(edge.getRight()==3);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==2);
		assertTrue(edge.getRight()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==3);
		assertTrue(edge.getRight()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==2);
		assertTrue(edge.getRight()==5);
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
	
	@Test
	public void saveFileBasedNetwork() throws Exception {
		setResolvedMergeConflictScenario();
		Project aProject = new Project("http://github.com/test", db);
		ConflictBasedNetworkBuilder builder = new ConflictBasedNetworkBuilder();
		builder.setProject(aProject);
		builder.execute(NetworkType.FILE_BASED);
		
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
		
		assertTrue(edge.getLeft()==1);
		assertTrue(edge.getRight()==2);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==1);
		assertTrue(edge.getRight()==3);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==1);
		assertTrue(edge.getRight()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==2);
		assertTrue(edge.getRight()==3);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==2);
		assertTrue(edge.getRight()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==3);
		assertTrue(edge.getRight()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==5);
		assertTrue(edge.getRight()==4);
		assertTrue(edge.getNetworkID()==connet.getID());
		edge = iEdges.next();
		assertTrue(edge.getLeft()==5);
		assertTrue(edge.getRight()==2);
		assertTrue(edge.getNetworkID()==connet.getID());
		assertFalse(iEdges.hasNext());
		
		// assert developers saved
		DeveloperNodeDAO ddao = (DeveloperNodeDAO) DAOFactory.getDAO(CotonetBean.NODE);
		Iterator<DeveloperNode> iNodes = ddao.list().iterator();
		DeveloperNode node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("deva@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("deve@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devb@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devc@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		node = iNodes.next();
		assertTrue(node.equals(new DeveloperNode("devd@project.com")));
		assertTrue(node.getSystemID()==aProject.getID());
		assertFalse(iNodes.hasNext());
	}
	
}
