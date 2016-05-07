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
package br.com.riselabs.cotonet.model.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.dao.validators.DeveloperEdgeValidator;
import br.com.riselabs.cotonet.model.db.DBConnection;
import br.com.riselabs.cotonet.model.db.DBManager;

/**
 * @author Alcemir R. Santos
 *
 */
public class DeveloperEdgeDAO implements DAO<DeveloperEdge> {

	private Connection conn;

	public DeveloperEdgeDAO() { }
	
	@Override
	public boolean save(DeveloperEdge edge) throws IllegalArgumentException {
		DeveloperEdgeValidator validator =  new DeveloperEdgeValidator();
		boolean hasSaved = false;
		if (validator.validate(edge)) {
			PreparedStatement ps;
			try {
				conn = (conn==null || conn.isClosed())?DBConnection.getConnection():conn;
				ps = conn
						.prepareStatement("insert into `edges` (`network_id`, `dev_a`, `dev_b`, `weight`) values (?,?,?,?);");
				ps.setInt(1, edge.getNetworkID());
				ps.setInt(2, edge.getLeft());
				ps.setInt(3, edge.getRight());
				ps.setInt(4, edge.getWeight());
				hasSaved = DBManager.executeUpdate(ps);
			} catch (SQLException | ClassNotFoundException | IOException e) {
				DBConnection.closeConnection(conn);
				e.printStackTrace();
			}
		}else{
			throw new IllegalArgumentException("The developer edge was invalid.");
		}
		if(conn!=null){ 
			DBConnection.closeConnection(conn);
		}
		return hasSaved;
	}

	@Override
	public void delete(DeveloperEdge object) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<DeveloperEdge> list() throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeveloperEdge get(DeveloperEdge edge)
			throws IllegalArgumentException {
		try {
			conn = (conn==null || conn.isClosed())?DBConnection.getConnection():conn;
			PreparedStatement ps = conn.prepareStatement("select * from `edges` where (`network_id`=? and `dev_a`=? and `dev_b`=?) or `id`=?;");
			
			if (edge.getNetworkID()==null) {
				ps.setInt(1, Integer.MAX_VALUE);
			}else{
				ps.setInt(1, edge.getNetworkID());
			}
			if (edge.getLeft()==null) {
				ps.setInt(2, Integer.MAX_VALUE);
			}else{
				ps.setInt(2, edge.getLeft());
			}
			if (edge.getRight()==null) {
				ps.setInt(3, Integer.MAX_VALUE);
			}else{
				ps.setInt(3, edge.getRight());
			}
			if (edge.getID()==null) {
				ps.setInt(4, Integer.MAX_VALUE);
			}else{
				ps.setInt(4, edge.getID());
			}
			ResultSet rs = DBManager.executeQuery(ps);
			if(rs.next()){
				Integer id = rs.getInt("id");
				Integer networkid = rs.getInt("network_id");
				Integer left = rs.getInt("dev_a");
				Integer right = rs.getInt("dev_b");
				DeveloperEdge nodeResult = new DeveloperEdge(id, networkid, left, right);
				DBConnection.closeConnection(conn);
				return nodeResult;
			}
		} catch (SQLException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		if(conn!=null){ 
			DBConnection.closeConnection(conn);
		}
		return null;
	}

	@Override
	public List<DeveloperEdge> search(DeveloperEdge object)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

}
