package br.com.riselabs.crawlers.db;

import java.io.File;
import java.sql.*;

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
				Class.forName("com.mysql.jdbc.Driver");
				String dbURL = "jdbc:mysql://localhost/ghanalysis";
				String user = "root";
				String pass = "root";
				// Setup the connection with the DB
				conn = DriverManager.getConnection(dbURL, user, pass);
				break;
			}
			// Retornando a conexão
			return conn;
		} catch (ClassNotFoundException e) {
			// Tratando caso não carregue o driver
			String errorMsg = "Driver not found.";
			throw new ClassNotFoundException(errorMsg, e);
		} catch (SQLException e) {
			// Tratando casso não consiga obter a conexão
			String errorMsg = "Conncection error.";
			throw new ClassNotFoundException(errorMsg, e);
		}
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
