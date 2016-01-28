/**
 * 
 */
package br.com.riselabs.crawlers.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.crawlers.db.DBConnection;

/**
 * @author Alcemir R. Santos
 *
 */
public class CrawlerDBManagerTest {

	private Connection conn;
	
	@Before
	public void setup() throws ClassNotFoundException{
		conn = DBConnection.getConnection();
	}
	
	@After
	public void teardown() throws SQLException{
		if (!conn.isClosed()) conn.close();
			
	}
	
	// connect to the mysql DB;
	@Test
	public void openMySQLConnection(){
		assertNotNull(conn);
	}

	// close connection to the mysql DB;
	@Test
	public void closeMySQLConnection() throws SQLException  {
		DBConnection.closeConnection(conn);
		assertTrue(conn.isClosed());
	}

	// execute a select query;
	@Test
	public void executeSelectQuery() throws ClassNotFoundException, SQLException {
		PreparedStatement statement = conn.prepareStatement("select count(*) \"count\" from ghanalysis.repository");
		ResultSet a = statement.executeQuery();
		a.first();
		assertEquals("there should be 711 entries", 711, a.getShort("count"));
		a.close();
	}

}
