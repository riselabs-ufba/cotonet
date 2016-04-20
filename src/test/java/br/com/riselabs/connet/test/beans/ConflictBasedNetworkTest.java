package br.com.riselabs.connet.test.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jgit.junit.RepositoryTestCase;
import org.junit.Test;

import br.com.riselabs.connet.beans.ConflictBasedNetwork;
import br.com.riselabs.connet.beans.DeveloperNode;

public class ConflictBasedNetworkTest extends RepositoryTestCase {

	@Test
	public void emptyEdges() {
		fail("not implemented yet.");
	}
	
	@Test
	public void emptyNodes() {
		fail("not implemented yet.");
	}

	@Test
	public void withEdgesButEmptyNodes() {
		fail("not implemented yet.");
	}
	
	@Test
	public void getNodeByNameAndEmail(){
		ConflictBasedNetwork connet = new ConflictBasedNetwork();
		assertTrue(connet.getNodes().isEmpty());
		String aName = "DevA";
		String anEmail = "deva@project.com";
		DeveloperNode aNode = new DeveloperNode(1,aName, anEmail);
		connet.add(aNode);
		assertEquals(1, connet.getNodes().size());
		DeveloperNode resultingNode = connet.getNode(aName, anEmail);
		assertNotNull("the node should not be null.", resultingNode);
		assertTrue("both nodes should be equal.", resultingNode.equals(aNode));
	}

}
