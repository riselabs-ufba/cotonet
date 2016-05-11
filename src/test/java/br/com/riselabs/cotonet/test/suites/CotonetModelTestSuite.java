package br.com.riselabs.cotonet.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.cotonet.test.model.DBManagerTest;
import br.com.riselabs.cotonet.test.model.beans.ConflictBasedNetworkTest;
import br.com.riselabs.cotonet.test.model.beans.DeveloperEdgeTest;
import br.com.riselabs.cotonet.test.model.beans.DeveloperNodeTest;
import br.com.riselabs.cotonet.test.model.dao.ConflictBasedNetworkDAOTest;
import br.com.riselabs.cotonet.test.model.dao.DeveloperEdgeDAOTest;
import br.com.riselabs.cotonet.test.model.dao.DeveloperNodeDAOTest;
import br.com.riselabs.cotonet.test.model.dao.MergeScenarioDAOTest;
import br.com.riselabs.cotonet.test.model.dao.ProjectDAOTest;
import br.com.riselabs.cotonet.test.model.handlers.FilesWritingTest;

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
	DBManagerTest.class
})
/**
 * @author Alcemir R. Santos
 *
 */
public class CotonetModelTestSuite {

}
