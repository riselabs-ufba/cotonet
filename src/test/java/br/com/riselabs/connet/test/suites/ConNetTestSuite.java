/**
 * 
 */
package br.com.riselabs.connet.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.connet.test.ConflictBasedNetworkBuilderTest;
import br.com.riselabs.connet.test.ConflictBasedNetworkTest;
import br.com.riselabs.connet.test.beans.DeveloperEdgeTest;
import br.com.riselabs.connet.test.beans.DeveloperNodeTest;
import br.com.riselabs.connet.test.commands.RecursiveBlameTest;

/**
 * @author alcemirsantos
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	DeveloperNodeTest.class,
	DeveloperEdgeTest.class,
	RecursiveBlameTest.class,
	ConflictBasedNetworkTest.class,
	ConflictBasedNetworkBuilderTest.class
})
public class ConNetTestSuite {

}
