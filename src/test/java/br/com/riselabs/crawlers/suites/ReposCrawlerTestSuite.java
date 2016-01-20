/**
 * 
 */
package br.com.riselabs.crawlers.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.crawlers.CrawlerDBManagerTest;
import br.com.riselabs.crawlers.GitRepositoryHandlerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CrawlerDBManagerTest.class,
	GitRepositoryHandlerTest.class
})
/**
 * @author Alcemir R. Santos
 *
 */
public class ReposCrawlerTestSuite {

}
