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
							+ "(`system_id`, `commit_base`,`commit_left`,`commit_right`, `commit_merge`) "
							+ "values (?,?,?,?,?);");
			ps.setInt(1, ms.getProjectID());
			ps.setInt(2, ms.getBaseID());
			ps.setInt(3, ms.getLeftID());
			ps.setInt(4, ms.getRightID());
			ps.setInt(5, ms.getMergeID());
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
				ms.setBaseID(rs.getInt("commit_base"));
				ms.setLeftID(rs.getInt("commit_left"));
				ms.setRightID(rs.getInt("commit_right"));
				ms.setMergeID(rs.getInt("commit_merge"));
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
//				if (ms.getBase() == null && ms.getLeft() == null
//						&& ms.getRight() == null) {
					if (ms.getBaseID() == null && ms.getLeftID() == null
							&& ms.getRightID() == null) {
						ps.setInt(1, Integer.MAX_VALUE);
						ps.setInt(2, Integer.MAX_VALUE);
						ps.setInt(3, Integer.MAX_VALUE);
					} else {
						ps.setInt(1, ms.getBaseID());
						ps.setInt(2, ms.getLeftID());
						ps.setInt(3, ms.getRightID());
					}
//				} else {
//					CommitDAO tmpDAO = (CommitDAO) DAOFactory.getDAO(CotonetBean.COMMIT);
//					
//					Commit tmp = new Commit(ms.getBase().getName());
//					ps.setInt(1, tmpDAO.get(tmp).getID());
//					tmp = new Commit(ms.getLeft().getName());
//					ps.setInt(2, tmpDAO.get(tmp).getID());
//					tmp = new Commit(ms.getRight().getName());
//					ps.setInt(3, tmpDAO.get(tmp).getID());
//				}
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
				Integer left = rs.getInt("commit_left");
				Integer base = rs.getInt("commit_base");
				Integer right = rs.getInt("commit_right");
				Integer merge = rs.getInt("commit_merge");
				MergeScenario pResult = new MergeScenario(id, systemID, base,
						left, right, merge);
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
