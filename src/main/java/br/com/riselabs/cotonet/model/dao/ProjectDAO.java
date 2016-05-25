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
import java.util.List;

import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.validators.ProjectValidator;
import br.com.riselabs.cotonet.model.db.Database;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class ProjectDAO implements DAO<Project> {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;
	
	private File log = null;
	
	public ProjectDAO(){
	}
	
	public void setLog(File f) {
		log = f;
	}

	/**
	 * Inserts a <code>{@link Project}</code> in the database.
	 * 
	 * @return - {@code true} whether it was possible persist the project,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean save(Project p) throws InvalidCotonetBeanException {
		ProjectValidator validator = new ProjectValidator();
		boolean hasSaved = false;
		validator.validate(p);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `systems` (`name`, `url`) values (?,?);");
			ps.setString(1, p.getName());
			ps.setString(2, p.getUrl());
			hasSaved = ps.executeUpdate() > 0 ? true : false;
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
	public void delete(Project p) throws InvalidCotonetBeanException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"delete from `systems` whrere `id`=?;");
			ps.setInt(1, p.getID());
			ps.executeUpdate();
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
	}

	@Override
	public List<Project> list() throws InvalidCotonetBeanException {
		return null;
	}

	@Override
	public Project get(Project p) throws InvalidCotonetBeanException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"select * from `systems` where `url`=? or `id`=?;");
			ps.setString(1, p.getUrl());
			if (p.getID() == null) {
				ps.setInt(2, Integer.MAX_VALUE);
			} else {
				ps.setInt(2, p.getID());
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String url = rs.getString("url");
				Project pResult = new Project(id, name, url, null);
				return pResult;
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
	public List<Project> search(Project object)
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
