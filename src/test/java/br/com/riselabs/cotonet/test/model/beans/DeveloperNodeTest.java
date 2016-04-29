package br.com.riselabs.cotonet.test.model.beans;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.riselabs.cotonet.model.beans.DeveloperNode;

public class DeveloperNodeTest {

	@Test
	public void equals(){
		DeveloperNode a = new DeveloperNode();
		a.setEmail("a@d.com");
		a.setName("a");
		DeveloperNode b = new DeveloperNode();
		b.setEmail("a@d.com");
		b.setName("a");
		assertTrue(a.equals(b));
	}
	
}
