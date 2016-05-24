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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.dao.validators.MergeScenarioValidator;
import br.com.riselabs.cotonet.model.db.DBConnection;
import br.com.riselabs.cotonet.model.db.DBManager;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class MergeScenarioDAO implements DAO<MergeScenario> {

	public MergeScenarioDAO() {
	}

	@Override
	public boolean save(MergeScenario ms) throws InvalidCotonetBeanException {
		MergeScenarioValidator validator = new MergeScenarioValidator();
		boolean hasSaved = false;
		validator.validate(ms);
		try {
			PreparedStatement ps = DBManager.getPreparedStatement(
					"insert into `merge_scenarios` "
					+ "(`system_id`, `commit_base`,`commit_left`,`commit_right`) "
					+ "values (?,?,?,?);");
			ps.setInt(1, ms.getProjectID());
			ps.setString(2, ms.getBase().getName());
			ps.setString(3, ms.getLeft().getName());
			ps.setString(4, ms.getRight().getName());
			hasSaved = DBManager.executeUpdate(ps);
		} catch (SQLException | ClassNotFoundException | IOException e) {
			Logger.logStackTrace(e);
		}finally{
			DBConnection.INSTANCE.closeConnection();
		}
		return hasSaved;
	}

	@Override
	public void delete(MergeScenario object) throws InvalidCotonetBeanException {
		try {
			PreparedStatement ps = DBManager.getPreparedStatement(
					"delete from `merge_cenarios` whrere `id`=?;");
			ps.setInt(1, object.getID());
			DBManager.executeUpdate(ps);
		} catch (Exception e) {
			Logger.logStackTrace(e);
		}finally{
			DBConnection.INSTANCE.closeConnection();
		}
	}

	@Override
	public List<MergeScenario> list() throws InvalidCotonetBeanException {
		List<MergeScenario> result = new ArrayList<MergeScenario>();
		try {
			PreparedStatement ps = DBManager.getPreparedStatement(
					"select * from `merge_scenarios`;");
			ResultSet rs = DBManager.executeQuery(ps);

			while (rs.next()) {
				MergeScenario ms = new MergeScenario();
				ms.setID(rs.getInt("id"));
				ms.setProjectID(rs.getInt("system_id"));
				ms.setSHA1Base(rs.getString("commit_base"));
				ms.setSHA1Left(rs.getString("commit_left"));
				ms.setSHA1Right(rs.getString("commit_right"));
				result.add(ms);
			}

		} catch (SQLException | IOException | ClassNotFoundException e) {
			Logger.logStackTrace(e);
		}finally{
			DBConnection.INSTANCE.closeConnection();
		}
		return result;
	}

	@Override
	public MergeScenario get(MergeScenario ms)
			throws InvalidCotonetBeanException {
		try {
			PreparedStatement ps = DBManager.getPreparedStatement(
					"select * from `merge_scenarios`"
					+ " where (`commit_base`=? and `commit_left`=? and `commit_right`=?) "
					+ "or `id`=?;");
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
			if (ms.getID() == null) {
				ps.setInt(4, Integer.MAX_VALUE);
			} else {
				ps.setInt(4, ms.getID());
			}
			ResultSet rs = DBManager.executeQuery(ps);
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer systemID = rs.getInt("system_id");
				String left = rs.getString("commit_left");
				String base = rs.getString("commit_base");
				String right = rs.getString("commit_right");
				MergeScenario pResult = new MergeScenario(id, systemID, base,
						left, right);
				return pResult;
			}
		} catch (SQLException | ClassNotFoundException | IOException e) {
			Logger.logStackTrace(e);
		}finally{
			DBConnection.INSTANCE.closeConnection();
		}
		return null;
	}

	@Override
	public List<MergeScenario> search(MergeScenario object)
			throws InvalidCotonetBeanException {
		// TODO Auto-generated method stub
		return null;
	}

}
