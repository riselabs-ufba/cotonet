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

import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.dao.validators.DeveloperNodeValidator;
import br.com.riselabs.cotonet.model.db.DBConnection;
import br.com.riselabs.cotonet.model.db.DBManager;

/**
 * @author Alcemir R. Santos
 *
 */
public class DeveloperNodeDAO implements DAO<DeveloperNode> {

	private Connection conn;

	public DeveloperNodeDAO () { }
	
	@Override
	public boolean save(DeveloperNode node) throws IllegalArgumentException {
		DeveloperNodeValidator validator = new DeveloperNodeValidator();
		boolean hasSaved = false;
		if (validator.validate(node)) {
			try {
				conn = (conn==null || conn.isClosed())?DBConnection.getConnection():conn;
				PreparedStatement ps = conn
						.prepareStatement("insert into `developers` (`name`, `email1`, `system_id`) values (?,?,?);");
				ps.setString(1, node.getName());
				ps.setString(2, node.getEmail());
				ps.setInt(3, node.getSystemID());
				hasSaved = DBManager.executeUpdate(ps);
			} catch (SQLException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}

		} else {
			throw new IllegalArgumentException("The developer node was not valid.");
		}
		if(conn!=null){ 
			DBConnection.closeConnection(conn);
		}
		return hasSaved;
	}

	@Override
	public void delete(DeveloperNode object) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<DeveloperNode> list() throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeveloperNode get(DeveloperNode node)
			throws IllegalArgumentException {
		try {
			conn = (conn==null || conn.isClosed())?DBConnection.getConnection():conn;
			PreparedStatement ps = conn.prepareStatement("select * from `developers` where `email1`=? or `id`=?;");
			ps.setString(1, node.getEmail());
			if (node.getID()==null) {
				ps.setInt(2, Integer.MAX_VALUE);
			}else{
				ps.setInt(2, node.getID());
			}
			ResultSet rs = DBManager.executeQuery(ps);
			if(rs.next()){
				Integer id = rs.getInt("id");
				Integer systemid = rs.getInt("system_id");
				String name = rs.getString("name");
				String email = rs.getString("email1");
				DeveloperNode nodeResult = new DeveloperNode(id, systemid, name, email);
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
	public List<DeveloperNode> search(DeveloperNode object)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

}
