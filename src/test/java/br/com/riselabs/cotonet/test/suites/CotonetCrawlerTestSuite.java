package br.com.riselabs.cotonet.test.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import br.com.riselabs.cotonet.test.model.CrawlerDBManagerTest;
import br.com.riselabs.cotonet.test.model.handlers.FilesWritingTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CrawlerDBManagerTest.class,
	FilesWritingTest.class
})
/**
 * @author Alcemir R. Santos
 *
 */
public class CotonetCrawlerTestSuite {

}
