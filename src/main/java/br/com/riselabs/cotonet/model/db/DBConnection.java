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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 
 * @author Alcemir R. Santos
 *
 */
public enum DBConnection {
	INSTANCE;
	
	private Properties prop = new Properties();
	private InputStream input = null;

	private static Connection conn;
	
	public enum TypeConnection {
		MYSQL, SQLITE
	}

	/**
	 * Returns a default MySQL connection.
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public synchronized  Connection getConnection() throws ClassNotFoundException, IOException, SQLException {
		if (conn==null|| conn.isClosed()) {
			conn = openConnection(TypeConnection.MYSQL);
		}
		return conn;
	}

	/**
	 * Returns a connection of a given type.
	 * 
	 * @param type
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException 
	 */
	public synchronized  Connection getConnection(TypeConnection type) throws ClassNotFoundException, IOException, SQLException {
		if (conn==null|| conn.isClosed()) {
			conn = openConnection(type);
		}
		return conn;
	}

	
	public synchronized  Connection openConnection(TypeConnection type)
			throws ClassNotFoundException, IOException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		input = classloader.getResourceAsStream("db.properties");
		prop.load(input);
		
		String db = prop.getProperty("database.name");
		String user = prop.getProperty("database.user");
		String pass = prop.getProperty("database.password");
		
		try {
			switch (type) {
			case SQLITE:
				// Carregando o driver
				Class.forName("org.sqlite.JDBC");
				// Estabelecendo conexão
				conn = DriverManager.getConnection("jdbc:sqlite:" +db);
				break;
			case MYSQL:
			default:
				// This will load the MySQL driver, each DB has its own driver
				String dbClass = "com.mysql.jdbc.Driver";
				Class.forName(dbClass).newInstance();
				String dbURL = "jdbc:mysql://localhost/"+db+"?autoReconnect=true&useSSL=false";
				// Setup the connection with the DB
				conn = DriverManager.getConnection(dbURL, user, pass);
				break;
			}
		} catch (ClassNotFoundException e) {
			// Tratando caso não carregue o driver
			String errorMsg = "Driver not found.";
			throw new ClassNotFoundException(errorMsg, e);
		} catch (SQLException e) {
			// Tratando casso não consiga obter a conexão
			String errorMsg = "Conncection error.";
			throw new ClassNotFoundException(errorMsg, e);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		// Retornando a conexão
		return conn;
	}

	public synchronized void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"
					+ e.getCause());
		}
	}

	public synchronized void closeConnection(Statement stm) {
		try {
			stm.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"
					+ e.getCause());
		}
		closeConnection();
	}

	public synchronized void closeConnection(Statement stm,	ResultSet rs) {
		try {
			rs.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"
					+ e.getCause());
		}
		closeConnection(stm);
	}

}
