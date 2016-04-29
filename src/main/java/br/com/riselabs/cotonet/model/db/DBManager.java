package br.com.riselabs.cotonet.model.db;

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
	 * Executa uma query de seleção eviada por um XxxxxxDAO.java
	 * @param sql
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	  public static ResultSet executeQuery(String sql) throws SQLException, ClassNotFoundException {
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
	 * @param result
	 * @param dbConnection
	 * @param query
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public static ResultSet executeQuery(PreparedStatement query) throws ClassNotFoundException {
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
	 * Executa uma query de inserção, remoção ou atualização eviada por um XxxxxxDAO.java
	 * 
	 * @param query
	 * @throws SQLException 
	 * @throws SQLException,ClassNotFoundException 
	 */
	  public static void executeUpdate(String query) throws SQLException, ClassNotFoundException {
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
		 * Executa uma query de inserção, remoção ou atualização eviada por um XxxxxxDAO.java
		 * 
		 * @param updateStmt
		 * @throws SQLException 
		 * @throws ClassNotFoundException 
		 */
	  public static void executeUpdate(PreparedStatement updateStmt) throws SQLException, ClassNotFoundException {
		  Connection dbConnection = DBConnection.getConnection();
		  try{
			  updateStmt.executeUpdate();
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
		}	
}
