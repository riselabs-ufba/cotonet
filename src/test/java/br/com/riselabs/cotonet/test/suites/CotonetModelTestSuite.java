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
package br.com.riselabs.cotonet.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.cotonet.test.model.beans.ConflictBasedNetworkTest;
import br.com.riselabs.cotonet.test.model.beans.DeveloperEdgeTest;
import br.com.riselabs.cotonet.test.model.beans.DeveloperNodeTest;
import br.com.riselabs.cotonet.test.model.dao.ConflictBasedNetworkDAOTest;
import br.com.riselabs.cotonet.test.model.dao.DeveloperEdgeDAOTest;
import br.com.riselabs.cotonet.test.model.dao.DeveloperNodeDAOTest;
import br.com.riselabs.cotonet.test.model.dao.MergeScenarioDAOTest;
import br.com.riselabs.cotonet.test.model.dao.ProjectDAOTest;
import br.com.riselabs.cotonet.test.model.db.DBManagerTest;
import br.com.riselabs.cotonet.test.model.db.DBWritterTest;
import br.com.riselabs.cotonet.test.model.handlers.FilesWritingTest;

/**
 * @author Alcemir R. Santos
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// Beans
	DeveloperNodeTest.class,
	DeveloperEdgeTest.class,
	ConflictBasedNetworkTest.class,
	// DAOs	
	ProjectDAOTest.class,
	ConflictBasedNetworkDAOTest.class,
	DeveloperNodeDAOTest.class,
	DeveloperEdgeDAOTest.class,
	MergeScenarioDAOTest.class,
	// Handlers
	FilesWritingTest.class,
	// Model
	DBManagerTest.class,
	DBWritterTest.class
})
public class CotonetModelTestSuite {

}
