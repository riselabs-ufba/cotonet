/**
 * 
 */
package br.com.riselabs.connet.test.beans;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.riselabs.connet.beans.DeveloperEdge;

/**
 * @author alcemirsantos
 *
 */
public class DeveloperEdgeTest {

	@Test
	public void bidiretionality(){
		DeveloperEdge a = new DeveloperEdge(1, 22);
		DeveloperEdge b = new DeveloperEdge(22, 1);
		assertTrue("edges are bidiretional. equals() should return true.", a.equals(b));
	}
}
