/**
 * 
 */
package br.com.riselabs.connet.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.connet.test.beans.ConflictBasedNetworkTest;
import br.com.riselabs.connet.test.beans.DeveloperEdgeTest;
import br.com.riselabs.connet.test.beans.DeveloperNodeTest;
import br.com.riselabs.connet.test.builders.ConflictBasedNetworkBuilderTest;
import br.com.riselabs.connet.test.commands.RecursiveBlameTest;

/**
 * @author alcemirsantos
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// Beans
	DeveloperNodeTest.class,
	DeveloperEdgeTest.class,
	ConflictBasedNetworkTest.class,
	// Commands
	RecursiveBlameTest.class,
	// Builders
	ConflictBasedNetworkBuilderTest.class
})
public class ConNetTestSuite {

}
