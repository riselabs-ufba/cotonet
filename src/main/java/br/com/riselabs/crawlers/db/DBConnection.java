package br.com.riselabs.crawlers.db;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;

/**
 * TODO Creates the connection with the database (MySQL).
 * 
 * @author Alcemir R. Santos
 *
 */
public class DBConnection {

	public static final String PRODUCTION = "info" + File.separator
			+ "simules-production.db";
	public static final String TEST = "info" + File.separator
			+ "simules-test.db";
	// indica o ambiente que está sendo utilizado. TEST ou PRODUCTION
	public static String ENVIROMENT = TEST;


	public static Map<String, String> readCredentials(){
		List<String> lines = IOHandler.readFile(new File(RCProperties.getWorkingDir()+"/db.credentials"));
		String user = "";
		String pass = "";
		for (String line : lines) {
			if (line.contains("user")){
				user = line.split(":")[1].replace(" ", "");
			}
			if (line.contains("password")) {
				pass = line.split(":")[1].replace(" ", "");
			}
		}
		Map<String, String> m = new HashMap<String, String>();
		m.put("user", user);
		m.put("password", pass);
		return m; 
	}
	
	public static void main(String[] argv) {

		System.out
				.println("-------- MySQL JDBC Connection Testing ------------");

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
		}

		System.out.println("MySQL JDBC Driver Registered!");
		Connection connection = null;

		Map<String, String> m = readCredentials();
		try {
			connection = DriverManager.getConnection(
					"jdbc:mysql://localhost/ghanalysis", m.get("user"), m.get("pass"));

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}

		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
	}

	public DBConnection() {
	}

	public enum TypeConnection {
		MYSQL, SQLITE
	}

	/**
	 * Returns a default MySQL connection.
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Connection getConnection() throws ClassNotFoundException {
		return getConnection(TypeConnection.MYSQL);
	}

	/**
	 * Returns a connection of a given type.
	 * 
	 * @param type
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Connection getConnection(TypeConnection type)
			throws ClassNotFoundException {
		// Variável de conexão
		Connection conn = null;
		try {
			switch (type) {
			case SQLITE:
				// Carregando o driver
				Class.forName("org.sqlite.JDBC");
				// Estabelecendo conexão
				conn = DriverManager.getConnection("jdbc:sqlite:" + ENVIROMENT);
				break;
			case MYSQL:
			default:
				// This will load the MySQL driver, each DB has its own driver
				String dbClass = "com.mysql.jdbc.Driver";
				Class.forName(dbClass).newInstance();
				String dbURL = "jdbc:mysql://localhost/ghanalysis";
				Map<String, String> m = readCredentials();
				// Setup the connection with the DB
				conn = DriverManager.getConnection(dbURL, m.get("user"), m.get("password"));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Retornando a conexão
		return conn;
	}

	public static void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"
					+ e.getCause());
		}
	}

	public static void closeConnection(Connection conn, Statement stm) {
		try {
			stm.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"
					+ e.getCause());
		}
		closeConnection(conn);
	}

	public static void closeConnection(Connection conn, Statement stm,
			ResultSet rs) {
		try {
			rs.close();
		} catch (SQLException e) {
			System.err.println("Não foi possível fechar a conexão!"
					+ e.getCause());
		}
		closeConnection(conn, stm);
	}

}
