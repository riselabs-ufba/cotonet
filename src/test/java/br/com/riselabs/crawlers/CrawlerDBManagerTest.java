/**
 * 
 */
package br.com.riselabs.crawlers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.Connection;

import org.junit.Test;

import br.com.riselabs.crawlers.db.DBConnection;

/**
 * @author Alcemir R. Santos
 *
 */
public class CrawlerDBManagerTest {

	// TODO [Test] connect to the mysql DB;
	@Test public void testMySQLConnection() throws ClassNotFoundException{
		Connection conn = DBConnection.getConnection();
		assertNotNull(conn);
	}
	
	// TODO [Test] execute a get repositories query;
	@Test public void testExecuteMySQL(){
		fail("not implemented yet.");
	}
	
	// TODO [Test] execute get merges scenarios;
	@Test public void testGetMergeScenariosFilter(){
		fail("not implemented yet.");
	}
	
	// TODO [Test] execute get merges scenarios with filter;
	@Test public void testGetMergeScenariosFiltered(){
		fail("not implemented yet.");
	}
}
