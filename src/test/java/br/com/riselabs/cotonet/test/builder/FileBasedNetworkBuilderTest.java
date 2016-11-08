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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.builder.FileBasedNetworkBuilder;
import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.test.helpers.ConflictBasedRepositoryTestCase;

/**
 * @author Alcemir R. Santos
 *
 */
public class FileBasedNetworkBuilderTest extends
		ConflictBasedRepositoryTestCase {

	private FileBasedNetworkBuilder builder;

	@Before
	public void setup() {
		builder = new FileBasedNetworkBuilder(new Project("", db));
	}

	@Test
	public void shouldRetriveFileBasedContributors() throws Exception {
		setCollaborationScenarioInTempRepository();
		builder.build();
		
		Collection<DeveloperNode> list = builder.getProject().getDevs().values();

		Iterator<DeveloperNode> i = list.iterator();
		DeveloperNode aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("Dev B", "devb@project.com")));
		assertTrue(aNode.getName().equals("Dev B"));
		assertTrue(aNode.getEmail().equals("devb@project.com"));
		aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("Dev C", "devc@project.com")));
		assertTrue(aNode.getName().equals("Dev C"));
		assertTrue(aNode.getEmail().equals("devc@project.com"));
		aNode = i.next();
		assertTrue(aNode.equals(new DeveloperNode("Dev A", "deva@project.com")));
		assertTrue(aNode.getName().equals("Dev A"));
		assertTrue(aNode.getEmail().equals("deva@project.com"));
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
	public void buildFileBasedNetwork() throws Exception {
		setCollaborationScenarioInTempRepository();
		Project aProject = new Project("", db);
		builder = new FileBasedNetworkBuilder(aProject);
		builder.setProject(aProject);

		builder.build();
		ConflictBasedNetwork connet = builder.getProject()
				.getConflictBasedNetworks().iterator().next();

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
		assertTrue(edge.equals(new DeveloperEdge(
				new DeveloperNode(1, null, null, null), 
				new DeveloperNode(2, null, null, null), null, null, null)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(
				new DeveloperNode(1, null, null, null), 
				new DeveloperNode(3, null, null, null), null, null, null)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(
				new DeveloperNode(1, null, null, null), 
				new DeveloperNode(4, null, null, null), null, null, null)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(
				new DeveloperNode(2, null, null, null), 
				new DeveloperNode(3, null, null, null), null, null, null)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(
				new DeveloperNode(2, null, null, null), 
				new DeveloperNode(4, null, null, null), null, null, null)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(
				new DeveloperNode(3, null, null, null), 
				new DeveloperNode(4, null, null, null), null, null, null)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(
				new DeveloperNode(5, null, null, null), 
				new DeveloperNode(4, null, null, null), null, null, null)));
		edge = iEdges.next();
		assertTrue(edge.equals(new DeveloperEdge(
				new DeveloperNode(5, null, null, null), 
				new DeveloperNode(2, null, null, null), null, null, null)));
		assertFalse(iEdges.hasNext());
	}

}
