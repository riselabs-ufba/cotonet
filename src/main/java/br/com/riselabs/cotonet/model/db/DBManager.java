package br.com.riselabs.cotonet.model.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Manage the querys to the database.
 * 
 * @author Alcemir R. Santos
 *
 */
public class DBManager {

	 /**
	 * Executes a SELECT from a given XxxxxxDAO.java
	 * 
	 * @param sql
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	  public static ResultSet executeQuery(String sql) throws SQLException, ClassNotFoundException, IOException {
		  ResultSet rs = null;
		  Connection dbConnection = DBConnection.getConnection();
		  PreparedStatement stm = dbConnection.prepareStatement(sql);
		  
		  try {
			  rs = stm.executeQuery();
		  } catch (SQLException e) {
			  e.printStackTrace();
			  if (dbConnection != null) {
				  try {
					  System.err.print("Transaction is being rolled back");
					  dbConnection.rollback();
				  }catch(SQLException excep) {
					  e.printStackTrace();
				  }
			  }
		  }
		  return rs;
	  }
	
	/**
	 *  Executes a SELECT from a given XxxxxxDAO.java
	 *  
	 * @param result
	 * @param dbConnection
	 * @param query
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static ResultSet executeQuery(PreparedStatement query) throws ClassNotFoundException, IOException {
		Connection dbConnection = DBConnection.getConnection();
		ResultSet result= null;
		
		try{
			  result = query.executeQuery();
		  }catch (SQLException e ) {
			  e.printStackTrace();
			  if (dbConnection != null) {
				  try {
					  System.err.print("Transaction is being rolled back");
					  dbConnection.rollback();
				  }catch(SQLException excep) {
					  e.printStackTrace();
				  }
			  }
		  }
		return result;
	}

	/**
	 * Executes an INSERT, UPDATE, or DELETE query from a XxxxxxDAO.java
	 * 
	 * @param query
	 * @throws SQLException 
	 * @throws SQLException,ClassNotFoundException 
	 * @throws IOException 
	 */
	  public static void executeUpdate(String query) throws SQLException, ClassNotFoundException, IOException {
		  Connection dbConnection = DBConnection.getConnection();
		  Statement stm = dbConnection.createStatement();
		  try {
			  stm = dbConnection.createStatement();
			  stm.executeUpdate(query);
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
	  }
	  
	  /**
		 * Executes an INSERT, UPDATE, or DELETE query from a XxxxxxDAO.java
		 * 
		 * @param updateStmt
	 * @return 
		 * @throws SQLException 
		 * @throws ClassNotFoundException 
	 * @throws IOException 
		 */
	  public static boolean executeUpdate(PreparedStatement updateStmt) throws SQLException, ClassNotFoundException, IOException {
		  Connection dbConnection = DBConnection.getConnection();
		  int result = 0;
		  try{
			  result = updateStmt.executeUpdate();
		  }catch (SQLException e ) {
			  e.printStackTrace();
			  if (dbConnection != null) {
				  try {
					  System.err.print("Transaction is being rolled back");
					  dbConnection.rollback();
				  }catch(SQLException excep) {
					  e.printStackTrace();
				  }
			  }
		  }
		  return result>0?true:false;
		}	
}
