/**
 * 
 */
package br.com.riselabs.connet.test.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.riselabs.connet.beans.DeveloperEdge;
/**
 * @author alcemirsantos
 *
 */
public class DeveloperEdgeTest {
	
	@Test
	public void indeedEquals(){
		DeveloperEdge[] edges = {new DeveloperEdge(1, 1), new DeveloperEdge(1, 1)};
		assertTrue("edges are bidiretional. equals() should return true.", edges[0].equals(edges[1]));
	}
	
	@Test
	public void bidiretionality(){
		DeveloperEdge[] edges = {new DeveloperEdge(1, 2), new DeveloperEdge(2, 1)};
		assertTrue("edges are bidiretional. equals() should return true.", edges[0].equals(edges[1]));
	}
	
	@Test
	public void devToDiffeferent(){
		DeveloperEdge[] edges = {new DeveloperEdge(1, 1), new DeveloperEdge(1, 2)};
		assertFalse("dev to is different. equals() should return false.", edges[0].equals(edges[1]));
	}
	
	@Test
	public void devFromDiffeferent(){
		DeveloperEdge[] edges =	{new DeveloperEdge(1, 3), new DeveloperEdge(22, 3)};
		assertFalse("dev from is different. equals() should return false.", edges[0].equals(edges[1]));
	}
	
	@Test
	public void bothDiffeferent(){
		DeveloperEdge[] edges =	{new DeveloperEdge(1, 22), new DeveloperEdge(21, 3)};
		assertFalse("both devs are different. equals() should return false.", edges[0].equals(edges[1]));
	}
}
