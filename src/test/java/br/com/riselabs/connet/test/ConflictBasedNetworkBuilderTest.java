/**
 * 
 */
package br.com.riselabs.connet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.merge.MergeStrategy;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.connet.beans.ConflictBasedNetwork;
import br.com.riselabs.connet.beans.ConflictBasedNetwork.NetworkType;
import br.com.riselabs.connet.beans.Blame;
import br.com.riselabs.connet.beans.DeveloperEdge;
import br.com.riselabs.connet.beans.DeveloperNode;
import br.com.riselabs.connet.beans.JGitMergeScenario;
import br.com.riselabs.connet.beans.Project;
import br.com.riselabs.connet.builders.ConflictBasedNetworkBuilder;
import br.com.riselabs.connet.commands.RecursiveBlame;
import br.com.riselabs.connet.test.helpers.ConflictBasedRepositoryTestCase;

/**
 * @author alcemirsantos
 *
 */
public class ConflictBasedNetworkBuilderTest extends ConflictBasedRepositoryTestCase {

	private Git git;
	private ConflictBasedNetworkBuilder builder;

	@Before
	public void setup() throws Exception {
		super.setUp();
		git = Git.wrap(db);
		builder = new ConflictBasedNetworkBuilder();
	}

	@Test
	public void buildDefaultNetwork() throws Exception {
		JGitMergeScenario aScenario = setCollaborationScenario(true);
		Project aProject = new Project("http://gitrepos.com/test", db);
		
		builder.setProject(aProject);
		ConflictBasedNetwork connet = builder.buildConflictBasedNetwork(aScenario);
		
		assertTrue(connet.check());
		List<DeveloperNode> nodes = connet.getNodes();
		List<DeveloperEdge> edges = connet.getEdges();
		assertFalse("the scenario setted adds collaboration. nodes should not be empty.", nodes.isEmpty());
		assertFalse("the scenario setted adds collaboration. edges should not be empty.", edges.isEmpty());
		DeveloperNode devA = connet.getNode("Dev A", "deva@project.com");
		DeveloperNode devB = connet.getNode("Dev B", "devb@project.com");
		DeveloperNode devC = connet.getNode("Dev C", "devc@project.com");
		DeveloperNode devD = connet.getNode("Dev D", "devd@project.com");
		assertNotNull(devA);
		assertNotNull(devB);
		assertNotNull(devC);
		assertNotNull(devD);
		assertTrue(edges.contains(new DeveloperEdge(devA.getId(), devC.getId())));
		assertTrue(edges.contains(new DeveloperEdge(devB.getId(), devA.getId())));
		assertTrue(edges.contains(new DeveloperEdge(devD.getId(), devC.getId())));
	}

	@Test
	public void buildFileBasedNetwork() throws Exception {
		JGitMergeScenario aScenario = setCollaborationScenario(true);
		Project aProject = new Project("http://gitrepos.com/test", db);

		builder.setType(NetworkType.FILE_BASED);
		builder.setProject(aProject);
		ConflictBasedNetwork connet = builder.buildConflictBasedNetwork(aScenario);
		
		assertTrue(connet.check());
		assertFalse("the scenario setted adds collboration. nodes should not be empty.", connet.getNodes().isEmpty());
		assertFalse("the scenario setted adds collboration. edges should not be empty.", connet.getEdges().isEmpty());
	}

	@Test
	public void buildChunckBasedNetwork() throws Exception {
		JGitMergeScenario aScenario = setCollaborationScenario(true);
		Project aProject = new Project("http://gitrepos.com/test", db);
		builder.setType(NetworkType.CHUNK_BASED);
		
		builder.setProject(aProject);
		ConflictBasedNetwork connet = builder.buildConflictBasedNetwork(aScenario);
		
		assertTrue(connet.check());
		List<DeveloperNode> nodes = connet.getNodes();
		List<DeveloperEdge> edges = connet.getEdges();
		assertFalse("the scenario setted adds collboration. nodes should not be empty.", connet.getNodes().isEmpty());
		assertFalse("the scenario setted adds collboration. edges should not be empty.", connet.getEdges().isEmpty());
		assertTrue(nodes.contains(new DeveloperNode(5,"Dev E", "deve@project.com")));
	}

	@Test
	public void mergeConflictScenarioInFSSetting() throws Exception {
		JGitMergeScenario ms = setCollaborationScenario(true);

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
	public void mergeConflictScenarioInMemorySetting() throws Exception {
		JGitMergeScenario ms = setCollaborationScenario(false);

		MergeResult result = git.merge().setStrategy(MergeStrategy.RECURSIVE)
				.include("side", ms.getRight()).call();
		assertEquals(MergeStatus.CONFLICTING, result.getMergeStatus());
	}
	
	@Test
	public void shouldRetriveFooContributors() throws Exception {
		builder.setProject(new Project("", db));
		JGitMergeScenario scenario = setCollaborationScenario(true);
		RecursiveBlame blame = new RecursiveBlame();

		List<Blame> blames = blame.setRepository(db)
				.setBeginRevision(scenario.getLeft())
				.setEndRevision(scenario.getBase()).setFilePath("Foo.java")
				.call();
		List<DeveloperNode> list = builder.getDeveloperNodes(blames, scenario,
				null, null);

		Iterator<DeveloperNode> i = list.iterator();
		DeveloperNode aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("Dev E", "deve@project.com")));
		assertTrue(aNode.getName().equals("Dev E"));
		assertTrue(aNode.getEmail().equals("deve@project.com"));
		aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("Dev C", "devc@project.com")));
		assertTrue(aNode.getName().equals("Dev C"));
		assertTrue(aNode.getEmail().equals("devc@project.com"));
		assertFalse(i.hasNext());
	}
}
