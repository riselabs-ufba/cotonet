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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.dao.validators.MergeScenarioValidator;
import br.com.riselabs.cotonet.model.db.Database;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class MergeScenarioDAO implements DAO<MergeScenario> {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	private File log = null;

	public MergeScenarioDAO() {
	}

	public void setLog(File f) {
		log = f;
	}

	@Override
	public boolean save(MergeScenario ms) throws InvalidCotonetBeanException {
		MergeScenarioValidator validator = new MergeScenarioValidator();
		boolean hasSaved = false;
		validator.validate(ms);
		try {
			conn = Database.getConnection();
			ps = conn
					.prepareStatement("insert into `merge_scenarios` "
							+ "(`system_id`, `commit_base`,`commit_left`,`commit_right`, `commit_merge`, `merge_date`) "
							+ "values (?,?,?,?,?,?);");
			ps.setInt(1, ms.getProjectID());
			ps.setString(2, ms.getBase().getName());
			ps.setString(3, ms.getLeft().getName());
			ps.setString(4, ms.getRight().getName());
			ps.setString(5, ms.getMerge().getName());
			ps.setDate(6, new Date(ms.getMegeDate().getTime()));
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
	public void delete(MergeScenario object) throws InvalidCotonetBeanException {
		try {
			conn = Database.getConnection();
			ps = conn
					.prepareStatement("delete from `merge_cenarios` whrere `id`=?;");
			ps.setInt(1, object.getID());
			ps.executeUpdate();
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
	}

	@Override
	public List<MergeScenario> list() throws InvalidCotonetBeanException {
		return list(null);
	}

	public List<MergeScenario> list(Integer projectID) throws InvalidCotonetBeanException {
		List<MergeScenario> result = new ArrayList<MergeScenario>();
		try {
			conn = Database.getConnection();
			String sql = "select * from `merge_scenarios`;";
			if (projectID!=null) {
				sql = "select * from `merge_scenarios` where `system_id`=?;";
			}
			ps = conn.prepareStatement(sql);
			ps.setInt(1, projectID);
			rs = ps.executeQuery();

			while (rs.next()) {
				MergeScenario ms = new MergeScenario();
				ms.setID(rs.getInt("id"));
				ms.setProjectID(rs.getInt("system_id"));
				ms.setSHA1Base(rs.getString("commit_base"));
				ms.setSHA1Left(rs.getString("commit_left"));
				ms.setSHA1Right(rs.getString("commit_right"));
				ms.setSHA1Merge(rs.getString("commit_merge"));
				ms.setMegeDate(new Timestamp(rs.getDate("merge_date").getTime()));
				result.add(ms);
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
	public MergeScenario get(MergeScenario ms)
			throws InvalidCotonetBeanException {
		String sql;
		try {
			conn = Database.getConnection();
			// get with raw data
			if (ms.getID() == null) {
				sql = "select * from `merge_scenarios` where `commit_base`=? and `commit_left`=? and `commit_right`=?;";
				ps = conn.prepareStatement(sql);
				if (ms.getBase() == null && ms.getLeft() == null
						&& ms.getRight() == null) {
					if (ms.getSHA1Base() == null && ms.getSHA1Left() == null
							&& ms.getSHA1Right() == null) {
						ps.setString(1, "");
						ps.setString(2, "");
						ps.setString(3, "");
					} else {
						ps.setString(1, ms.getSHA1Base());
						ps.setString(2, ms.getSHA1Left());
						ps.setString(3, ms.getSHA1Right());
					}
				} else {
					ps.setString(1, ms.getBase().getName());
					ps.setString(2, ms.getLeft().getName());
					ps.setString(3, ms.getRight().getName());
				}
				// get with ID
			} else {
				sql = "select * from `merge_scenarios` where `id`=?;";
				ps = conn.prepareStatement(sql);
				if (ms.getID() == null) {
					ps.setInt(1, Integer.MAX_VALUE);
				} else {
					ps.setInt(1, ms.getID());
				}
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer systemID = rs.getInt("system_id");
				String left = rs.getString("commit_left");
				String base = rs.getString("commit_base");
				String right = rs.getString("commit_right");
				String merge = rs.getString("commit_merge");
				Timestamp mergeDate = new Timestamp(rs.getDate("merge_date").getTime());
				MergeScenario pResult = new MergeScenario(id, systemID, base,
						left, right, merge, mergeDate);
				return pResult;
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
	public List<MergeScenario> search(MergeScenario object)
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
