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
package br.com.riselabs.cotonet.test.model.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jgit.junit.RepositoryTestCase;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.dao.validators.ConflictBasedNetworkValidator;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

/**
 * 
 * @author Alcemir R. Santos
 *
 */
public class ConflictBasedNetworkTest extends RepositoryTestCase {

	private ConflictBasedNetwork connet;

	@Before
	public void setUp(){
		connet = new ConflictBasedNetwork();
	}
	
	@Test
	public void checkWithEdgesButEmptyNodes() {
		connet.add(new DeveloperEdge(new DeveloperNode(1, null, null, null),new DeveloperNode(2, null, null, null)
		));
		assertTrue(connet.getNodes().isEmpty());
		assertFalse(connet.getEdges().isEmpty());
		try {
			new ConflictBasedNetworkValidator().validate(connet);
		} catch (InvalidCotonetBeanException e) {
			assertTrue( e.getCause() instanceof IllegalArgumentException);
		}finally{
			fail("should have thrown invalid bean exception");
		}
	}
	
	@Test
	public void checkWithNodeButEmptyEdges() {
		connet.add(new DeveloperNode("test@mail"));
		assertFalse(connet.getNodes().isEmpty());
		assertTrue(connet.getEdges().isEmpty());
		try {
			new ConflictBasedNetworkValidator().validate(connet);
		} catch (InvalidCotonetBeanException e) {
			assertTrue( e.getCause() instanceof IllegalArgumentException);
		}finally{
			fail("should have thrown invalid bean exception");
		}
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
