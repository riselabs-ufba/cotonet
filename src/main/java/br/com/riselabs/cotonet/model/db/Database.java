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
package br.com.riselabs.cotonet.model.db;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public final class Database {

	private static final BasicDataSource ds = new BasicDataSource();

	private static void initDataSource(String dbname, String dbuser,
			String dbpass) throws IOException, SQLException,
			PropertyVetoException {
		String dbURL = "jdbc:mysql://localhost/"+ dbname +
				"?autoReconnect=true&useSSL=false&failOverReadOnly=false&maxReconnects=100";

		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUsername(dbuser);
		ds.setPassword(dbpass);
		ds.setUrl(dbURL);

		// the settings below are optional -- dbcp can work with defaults
		ds.setMinIdle(5);
		ds.setMaxIdle(20);
		ds.setMaxOpenPreparedStatements(180);

	}

	public static Connection getConnection() {
		Thread currentThread = Thread.currentThread();
		ClassLoader classloader = currentThread.getContextClassLoader();
		InputStream input = classloader.getResourceAsStream("db.properties");
		Properties prop = new Properties();
		try {
			if (input != null) {
				prop.load(input);
				input.close();
			} else {
				Logger.logStackTrace(new FileNotFoundException(
						"The file 'db.properties was not found."));
			}
		} catch (IOException e) {
			Logger.logStackTrace(e);
		}
		String db = prop.getProperty("database.name");
		String user = prop.getProperty("database.user");
		String pass = prop.getProperty("database.password");
		return getConnection(db, user, pass);
	}

	public static Connection getConnection(String dbname, String dbuser,
			String dbpass) {
		try {
			initDataSource(dbname, dbuser, dbpass);
			return ds.getConnection();
		} catch (SQLException | IOException | PropertyVetoException e) {
			Logger.logStackTrace(e);
		}
		return null;
	}

}