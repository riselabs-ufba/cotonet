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
public class DBConnection {
	
	private static Properties prop = new Properties();
	private static InputStream input = null;

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
	 * @throws IOException 
	 */
	public static Connection getConnection() throws ClassNotFoundException, IOException {
		return getConnection(TypeConnection.MYSQL);
	}

	/**
	 * Returns a connection of a given type.
	 * 
	 * @param type
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException 
	 */
	public static Connection getConnection(TypeConnection type)
			throws ClassNotFoundException, IOException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		input = classloader.getResourceAsStream("db.properties");
		prop.load(input);
		
		String db = prop.getProperty("database.name");
		String user = prop.getProperty("database.user");
		String pass = prop.getProperty("database.password");
		
		// Variável de conexão
		Connection conn = null;
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
