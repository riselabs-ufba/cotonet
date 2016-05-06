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
package br.com.riselabs.cotonet.test.model.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;

import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.DeveloperNodeDAO;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.test.helpers.ConflictBasedRepositoryTestCase;
import br.com.riselabs.cotonet.test.helpers.DBTestCase;

/**
 * @author Alcemir R. Santos
 *
 */
public class DeveloperNodeDAOTest extends ConflictBasedRepositoryTestCase{

	private DeveloperNodeDAO dao;
	
	@Before
	public void setup() throws ClassNotFoundException, URISyntaxException, SQLException, IOException{
		DBTestCase.resetTestDB();
		dao =  (DeveloperNodeDAO) DAOFactory.getDAO(CotonetBean.NODE);
		Project p =  new Project("http://github.com/test", null);
		((ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT)).save(p);
	}
	
	@After
	public void teardown() throws ClassNotFoundException, URISyntaxException, SQLException, IOException{
		DBTestCase.resetTestDB();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveEmptyNode(){
		DeveloperNode node = new DeveloperNode(); 
		assertNull(node.getName());
		assertNull(node.getEmail());
		assertNull(node.getSystemID());
		dao.save(node);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveWithoutEmail(){
		DeveloperNode node = new DeveloperNode(1, "dev", null); 
		assertTrue(node.getSystemID()==1);
		assertEquals("dev", node.getName());
		assertNull(node.getEmail());
		dao.save(node);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveWithEmptyEmail(){
		DeveloperNode node = new DeveloperNode(1, "dev", ""); 
		assertTrue(node.getSystemID()==1);
		assertEquals("dev", node.getName());
		assertEquals("", node.getEmail());
		dao.save(node);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveWithoutSystemID(){
		DeveloperNode node = new DeveloperNode(null, "dev", "dev@gmail.com"); 
		assertNull(node.getSystemID());
		assertEquals("dev", node.getName());
		assertEquals("dev@gmail.com", node.getEmail());
		dao.save(node);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveWithInvalidSystemID(){
		DeveloperNode node = new DeveloperNode(200, "dev", "dev@gmail.com"); 
		assertTrue(node.getSystemID()==200);
		assertEquals("dev", node.getName());
		assertEquals("dev@gmail.com", node.getEmail());
		dao.save(node);
	}
	
	@Test
	public void saveSuccesfully(){
		DeveloperNode node = new DeveloperNode(1, "dev", "dev@project.com");
		assertTrue(dao.save(node));
	}	
	
	@Test
	public void getDeveloper(){
		DeveloperNode node = new DeveloperNode(1, "dev", "dev@project.com");
		assertTrue(dao.save(node));
		
		DeveloperNode returnedDeveloper =  dao.get(node);
		assertNotNull(returnedDeveloper);
		assertTrue(returnedDeveloper.getID()==1);
		assertEquals("dev", returnedDeveloper.getName());
		assertEquals("dev@project.com", returnedDeveloper.getEmail());
		assertTrue(returnedDeveloper.getSystemID()==1);
	}
	
}
