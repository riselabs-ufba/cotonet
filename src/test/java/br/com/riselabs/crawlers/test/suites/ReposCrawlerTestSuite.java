/**
 * 
 */
package br.com.riselabs.crawlers.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.crawlers.test.CrawlerDBManagerTest;
import br.com.riselabs.crawlers.test.FilesWritingTest;
import br.com.riselabs.crawlers.test.GitRepositoryHandlerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CrawlerDBManagerTest.class,
	GitRepositoryHandlerTest.class,
	FilesWritingTest.class
})
/**
 * @author Alcemir R. Santos
 *
 */
public class ReposCrawlerTestSuite {

}
