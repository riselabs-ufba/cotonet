/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alcemir R. Santos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.com.riselabs.cotonet.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.model.db.DBConnection;
import br.com.riselabs.cotonet.model.db.DBManager;
import br.com.riselabs.cotonet.test.helpers.DBTestCase;

/**
 * @author Alcemir R. Santos
 *
 */
public class DBManagerTest extends DBTestCase {


	@Before
	public void setup() throws ClassNotFoundException, IOException, URISyntaxException, SQLException{
		conn = DBConnection.getConnection();
		resetTestDB();
	}

	@After
	public void teardown() throws SQLException {

	}

	// connect to the mysql DB;
	@Test
	public void openMySQLConnection() throws ClassNotFoundException,
			IOException {
		conn = DBConnection.getConnection();
		assertNotNull(conn);
	}

	// close connection to the mysql DB;
	@Test
	public void closeMySQLConnection() throws SQLException,
			ClassNotFoundException, IOException {
		conn = DBConnection.getConnection();
		conn.close();
		assertTrue(conn.isClosed());
	}

	// execute a select query;
	@Test
	public void executeSelectQuery() throws SQLException,
			ClassNotFoundException, IOException {
		conn = DBConnection.getConnection();
		PreparedStatement statement = conn
				.prepareStatement("select count(*) \"count\" from `cotonet-test`.`developers`;");
		ResultSet rs = DBManager.executeQuery(statement);
		rs.first();
		assertEquals("there should be 0 entries", 0, rs.getShort("count"));
		rs.close();
	}

	@Test
	public void executeInsertQuery() throws SQLException,
			ClassNotFoundException, IOException {
		conn = DBConnection.getConnection();
		PreparedStatement statement = conn
				.prepareStatement("insert into `cotonet-test`.`systems` (name, url) values (?,?), (?,?);");
		statement.setString(1, "test");
		statement.setString(2, "http://project.com/test");
		statement.setString(3, "test2");
		statement.setString(4, "http://project.com/test2");
		boolean hasInserted = DBManager.executeUpdate(statement);
		assertTrue(hasInserted);
		statement = conn
				.prepareStatement("select count(*) \"count\" from `cotonet-test`.`systems`;");
		ResultSet rs = DBManager.executeQuery(statement);
		rs.first();
		assertEquals("there should be 0 entries", 2, rs.getShort("count"));
		rs.close();
	}

	
}
