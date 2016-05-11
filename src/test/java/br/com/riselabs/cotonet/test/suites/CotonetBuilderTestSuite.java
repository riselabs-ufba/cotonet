/**
 * 
 */
package br.com.riselabs.cotonet.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.cotonet.test.builder.ConflictBasedNetworkBuilderTest;
import br.com.riselabs.cotonet.test.builder.command.GitConflictBlameTest;
import br.com.riselabs.cotonet.test.builder.command.RecursiveBlameTest;

/**
 * @author alcemirsantos
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	// Commands
	RecursiveBlameTest.class,
	GitConflictBlameTest.class,
	// Builders
	ConflictBasedNetworkBuilderTest.class
})
public class CotonetBuilderTestSuite {

}
