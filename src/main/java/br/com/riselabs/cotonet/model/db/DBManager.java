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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Manage the queries to the database.
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
