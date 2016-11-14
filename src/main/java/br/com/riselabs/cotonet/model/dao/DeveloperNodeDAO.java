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

import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.dao.validators.DeveloperNodeValidator;
import br.com.riselabs.cotonet.model.db.Database;
import br.com.riselabs.cotonet.model.enums.TypeDeveloper;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class DeveloperNodeDAO implements DAO<DeveloperNode> {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	private File log = null;

	public DeveloperNodeDAO() {
	}

	public void setLog(File f) {
		log = f;
	}

	@Override
	public boolean save(DeveloperNode node) throws InvalidCotonetBeanException {
		DeveloperNodeValidator validator = new DeveloperNodeValidator();
		boolean hasSaved = false;
		validator.validate(node);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `developers` "
					+ "(`name`, `email1`, `system_id`, `type`)"
					+ " values (?,?,?,?);");
			if (node.getName() == null) {
				node.setName(node.getEmail().trim().split("@")[0]);
			}
			ps.setString(1, node.getName());
			ps.setString(2, node.getEmail());
			ps.setInt(3, node.getSystemID());
			ps.setString(4, node.getType().toString());
			hasSaved = ps.executeUpdate() > 0 ? true : false;
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(log, e);
			}
			Logger.logStackTrace(log, e);
		} finally {
			closeResources();
		}
		return hasSaved;
	}

	@Override
	public void delete(DeveloperNode object) throws InvalidCotonetBeanException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<DeveloperNode> list() throws InvalidCotonetBeanException {
		List<DeveloperNode> result = new ArrayList<DeveloperNode>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `developers`;");
			rs = ps.executeQuery();

			while (rs.next()) {
				DeveloperNode node = new DeveloperNode();
				node.setID(rs.getInt("id"));
				node.setName(rs.getString("name"));
				node.setEmail(rs.getString("email1"));
				// edge.setRight(rs.getInt("email2"));
				node.setSystemID(rs.getInt("system_id"));
				node.setType(rs.getString("type").equals("A") ? TypeDeveloper.AUTHOR
						: TypeDeveloper.COMMITTER);
				result.add(node);
			}

		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(log, e);
			}
			Logger.logStackTrace(log, e);
		} finally {
			closeResources();
		}
		return result;
	}

	@Override
	public DeveloperNode get(DeveloperNode node)
			throws InvalidCotonetBeanException {
		try {
			conn = Database.getConnection();
			ps = conn
					.prepareStatement("select * from `developers` where `email1`=? or `id`=?;");
			ps.setString(1, node.getEmail());
			if (node.getID() == null) {
				ps.setInt(2, Integer.MAX_VALUE);
			} else {
				ps.setInt(2, node.getID());
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer systemid = rs.getInt("system_id");
				String name = rs.getString("name");
				String email = rs.getString("email1");
				String s = rs.getString("type");
				TypeDeveloper type = s.equals("A") ? TypeDeveloper.AUTHOR
						: TypeDeveloper.COMMITTER;
				DeveloperNode nodeResult = new DeveloperNode(id, systemid,
						name, email, type);
				return nodeResult;
			}
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(log, e);
			}
			Logger.logStackTrace(log, e);
		} finally {
			closeResources();
		}
		return null;
	}

	@Override
	public List<DeveloperNode> search(DeveloperNode object)
			throws InvalidCotonetBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeResources() {
		try {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			Logger.logStackTrace(log, e);
		}
	}

}
