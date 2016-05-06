/**
 * 
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
import org.junit.Before;
import org.junit.Ignore;
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
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.model.dao.validators.ConflictBasedNetworkValidator;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.test.helpers.ConflictBasedRepositoryTestCase;

/**
 * @author alcemirsantos
 *
 */
public class ConflictBasedNetworkBuilderTest extends
		ConflictBasedRepositoryTestCase {

	private Git git;
	private ConflictBasedNetworkBuilder builder;

	@Before
	public void setup() throws Exception {
		super.setUp();
		git = Git.wrap(db);
		builder = new ConflictBasedNetworkBuilder();
	}

	@Test
	public void buildFileBasedNetwork() throws Exception {
		MergeScenario aScenario = setCollaborationScenarioInTempRepository();
		Project aProject = new Project("", db);
		builder.setProject(aProject);
		
		MergeResult m = runMerge(aScenario);
		List<File> files = builder.getFilesWithConflicts(m);
		ConflictBasedNetwork connet = builder.build(aScenario, files, NetworkType.FILE_BASED);
		
		assertTrue(new ConflictBasedNetworkValidator().validate(connet));
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
		
		assertTrue(new ConflictBasedNetworkValidator().validate(connet));
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
	@Ignore
	public void saveFileBasedNetwork() throws Exception {
		setResolvedMergeConflictScenario();
		Project aProject = new Project("http://github.com/test", db);
		ConflictBasedNetworkBuilder builder = new ConflictBasedNetworkBuilder();
		builder.setProject(aProject);
		aProject = builder.execute(NetworkType.FILE_BASED);
		
		ProjectDAO dao = (ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT);
		assertTrue(dao.save(aProject));
	}

	@Test
	@Ignore
	public void saveChunckBasedNetwork() throws Exception {
		setResolvedMergeConflictScenario();
		Project aProject = new Project("http://github.com/test", db);
		ConflictBasedNetworkBuilder builder = new ConflictBasedNetworkBuilder();
		builder.setProject(aProject);
		aProject = builder.execute();
		
		ProjectDAO dao = (ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT);
		assertTrue(dao.save(aProject));
	}
}
