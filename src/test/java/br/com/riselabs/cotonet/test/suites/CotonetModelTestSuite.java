package br.com.riselabs.cotonet.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.cotonet.test.model.dao.ConflictBasedNetworkDAOTest;
import br.com.riselabs.cotonet.test.model.dao.DeveloperEdgeDAOTest;
import br.com.riselabs.cotonet.test.model.dao.DeveloperNodeDAOTest;
import br.com.riselabs.cotonet.test.model.dao.MergeScenarioDAOTest;
import br.com.riselabs.cotonet.test.model.dao.ProjectDAOTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ProjectDAOTest.class,
	ConflictBasedNetworkDAOTest.class,
	DeveloperNodeDAOTest.class,
	DeveloperEdgeDAOTest.class,
	MergeScenarioDAOTest.class
})
/**
 * @author Alcemir R. Santos
 *
 */
public class CotonetDAOsTestSuite {

}
