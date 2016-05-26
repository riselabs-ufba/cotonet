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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;

/**
 * 
 * @author Alcemir R. Santos
 *
 */
public class DeveloperEdgeTest {
	
	@Test
	public void indeedEquals(){
		DeveloperEdge a = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null));
		DeveloperEdge b = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null));
		DeveloperEdge[] edges = {a, b};
		assertTrue("edges are bidiretional. equals() should return true.", edges[0].equals(edges[1]));
	}
	
	@Test
	public void bidiretionality(){
		DeveloperEdge a = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null));
		DeveloperEdge b = new DeveloperEdge(new DeveloperNode(2, null, null, null), new DeveloperNode(1, null, null, null));
		DeveloperEdge[] edges = {a, b};
		assertTrue("edges are bidiretional. equals() should return true.", edges[0].equals(edges[1]));
	}
	
	@Test
	public void devToDiffeferent(){
		DeveloperEdge a = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null));
		DeveloperEdge b = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(22, null, null, null));
		DeveloperEdge[] edges = {a, b};
		assertFalse("dev to is different. equals() should return false.", edges[0].equals(edges[1]));
	}
	
	@Test
	public void devFromDiffeferent(){
		DeveloperEdge a = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null));
		DeveloperEdge b = new DeveloperEdge(new DeveloperNode(11, null, null, null), new DeveloperNode(2, null, null, null));
		DeveloperEdge[] edges =	{a, b};
		assertFalse("dev from is different. equals() should return false.", edges[0].equals(edges[1]));
	}
	
	@Test
	public void bothDiffeferent(){
		DeveloperEdge a = new DeveloperEdge(new DeveloperNode(1, null, null, null), new DeveloperNode(2, null, null, null));
		DeveloperEdge b = new DeveloperEdge(new DeveloperNode(11, null, null, null), new DeveloperNode(22, null, null, null));
		DeveloperEdge[] edges =	{a, b};
		assertFalse("both devs are different. equals() should return false.", edges[0].equals(edges[1]));
	}
}
