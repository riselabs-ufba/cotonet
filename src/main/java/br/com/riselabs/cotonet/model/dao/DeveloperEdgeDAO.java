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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.dao.validators.DeveloperEdgeValidator;
import br.com.riselabs.cotonet.model.db.Database;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class DeveloperEdgeDAO implements DAO<DeveloperEdge> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;
	
	private File log = null;
	
	public DeveloperEdgeDAO() {
	}

	public void setLog(File f) {
		log = f;
	}

	@Override
	public boolean save(DeveloperEdge edge) throws InvalidCotonetBeanException {
		DeveloperEdgeValidator validator = new DeveloperEdgeValidator();
		boolean hasSaved = false;
		validator.validate(edge);
		try {
			conn = Database.getConnection();
			ps =  conn.prepareStatement(
					"insert into `edges` (`network_id`, `dev_a`, `dev_b`, `weight`) values (?,?,?,?);");
			ps.setInt(1, edge.getNetworkID());
			ps.setInt(2, edge.getLeft());
			ps.setInt(3, edge.getRight());
			ps.setInt(4, edge.getWeight());
			hasSaved = ps.executeUpdate()>0?true:false;
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(log, e);
			}
			Logger.logStackTrace(log, e);
		}finally{
			closeResources();
		}
		return hasSaved;
	}

	@Override
	public void delete(DeveloperEdge object) throws InvalidCotonetBeanException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<DeveloperEdge> list() throws InvalidCotonetBeanException {
		List<DeveloperEdge> result = new ArrayList<DeveloperEdge>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"select * from `edges`;");
			rs = ps.executeQuery();

			while (rs.next()) {
				DeveloperEdge edge = new DeveloperEdge();
				edge.setID(rs.getInt("id"));
				edge.setNetworkID(rs.getInt("network_id"));
				edge.setLeft(rs.getInt("dev_a"));
				edge.setRight(rs.getInt("dev_b"));
				edge.setWeight(rs.getInt("weight"));
				result.add(edge);
			}

		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(log, e);
			}
			Logger.logStackTrace(log, e);
		}finally{
			closeResources();
		}
		return result;
	}

	@Override
	public DeveloperEdge get(DeveloperEdge edge)
			throws InvalidCotonetBeanException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"select * from `edges` where (`network_id`=? and `dev_a`=? and `dev_b`=?) or `id`=?;");

			if (edge.getNetworkID() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, edge.getNetworkID());
			}
			if (edge.getLeft() == null) {
				ps.setInt(2, Integer.MAX_VALUE);
			} else {
				ps.setInt(2, edge.getLeft());
			}
			if (edge.getRight() == null) {
				ps.setInt(3, Integer.MAX_VALUE);
			} else {
				ps.setInt(3, edge.getRight());
			}
			if (edge.getID() == null) {
				ps.setInt(4, Integer.MAX_VALUE);
			} else {
				ps.setInt(4, edge.getID());
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer networkid = rs.getInt("network_id");
				Integer left = rs.getInt("dev_a");
				Integer right = rs.getInt("dev_b");
				DeveloperEdge nodeResult = new DeveloperEdge(id, networkid,
						left, right);
				return nodeResult;
			}
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(log, e);
			}
			Logger.logStackTrace(log, e);
		}finally{
			closeResources();
		}
		return null;
	}

	@Override
	public List<DeveloperEdge> search(DeveloperEdge object)
			throws InvalidCotonetBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeResources() {
		try {
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
			if(conn!=null) conn.close();
		} catch (SQLException e) {
			Logger.logStackTrace(log, e);
		}
	}
}
