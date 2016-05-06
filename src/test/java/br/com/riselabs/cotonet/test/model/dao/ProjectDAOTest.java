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

import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.test.helpers.ConflictBasedRepositoryTestCase;
import br.com.riselabs.cotonet.test.helpers.DBTestCase;

/**
 * @author Alcemir R. Santos
 *
 */
public class ProjectDAOTest extends ConflictBasedRepositoryTestCase{

	private ProjectDAO dao;
	
	@Before
	public void setup(){
		dao =  (ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT);
	}
	
	@After
	public void teardown() throws ClassNotFoundException, URISyntaxException, SQLException, IOException{
		DBTestCase.resetTestDB();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveEmptyProject(){
		Project p =  new Project();
		assertNull(p.getName());
		assertNull(p.getUrl());
		assertTrue(p.getScenarioNetMap().isEmpty());
		dao.save(p);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveEmptyURL(){
		Project p =  new Project("");
		assertEquals("", p.getName());
		assertEquals("", p.getUrl());
		assertTrue(p.getScenarioNetMap().isEmpty());
		dao.save(p);
	}
	
	@Test
	public void saveSuccesfully(){
		Project p = new Project("http://hub.com/test", null);
		assertTrue(dao.save(p));
	}	
	
	@Test
	public void getProjectByURL(){
		Project p = new Project("http://hub.com/test", null);
		dao.save(p);
		
		Project returnedProject =  dao.get(p);
		assertNotNull(returnedProject);
		assertTrue(returnedProject.getID()==1);
		assertEquals("http://hub.com/test", returnedProject.getUrl());
	}
	
	@Test
	public void getProjectByID(){
		Project p = new Project("http://hub.com/test", null);
		dao.save(p);
		
		Project returnedProject =  dao.get(new Project(1, null, null, null));
		assertNotNull(returnedProject);
		assertTrue(returnedProject.getID()==1);
		assertEquals("http://hub.com/test", returnedProject.getUrl());
	}
	
}
