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

import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.dao.validators.ConflictBasedNetworkValidator;
import br.com.riselabs.cotonet.model.db.DBConnection;
import br.com.riselabs.cotonet.model.db.DBManager;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * 
 * @author Alcemir R. Santos
 *
 */
public class ConflictBasedNetworkDAO implements DAO<ConflictBasedNetwork> {

	public ConflictBasedNetworkDAO() {
	}

	@Override
	public boolean save(ConflictBasedNetwork conet)
			throws InvalidCotonetBeanException {
		ConflictBasedNetworkValidator validator = new ConflictBasedNetworkValidator();
		boolean hasSaved = false;
		validator.validate(conet);
		try {
			PreparedStatement ps = DBManager.getPreparedStatement(
					"insert into `networks` (`merge_scenario_id`, `type`) values (?,?);");
			ps.setInt(1, conet.getMergeScenarioID());
			ps.setString(2, conet.getType().toString());
			hasSaved = DBManager.executeUpdate(ps);
		} catch (SQLException | ClassNotFoundException | IOException e) {
			Logger.logStackTrace(e);
		}finally{
			DBConnection.INSTANCE.closeConnection();
		}
		return hasSaved;
	}

	@Override
	public void delete(ConflictBasedNetwork object)
			throws InvalidCotonetBeanException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ConflictBasedNetwork> list() throws InvalidCotonetBeanException {
		List<ConflictBasedNetwork> result = new ArrayList<ConflictBasedNetwork>();
		try {
			PreparedStatement ps = DBManager.getPreparedStatement(
					"select * from `networks`;");
			ResultSet rs = DBManager.executeQuery(ps);

			while (rs.next()) {
				ConflictBasedNetwork connet = new ConflictBasedNetwork();
				connet.setID(rs.getInt("id"));
				connet.setType(rs.getString("type").equals("C") ? NetworkType.CHUNK_BASED
						: NetworkType.FILE_BASED);
				connet.setMergeScenarioID(rs.getInt("merge_scenario_id"));
				result.add(connet);
			}

		} catch (SQLException | IOException | ClassNotFoundException e) {
			Logger.logStackTrace(e);
		}finally{
			DBConnection.INSTANCE.closeConnection();
		}
		return result;
	}

	@Override
	public ConflictBasedNetwork get(ConflictBasedNetwork conet)
			throws InvalidCotonetBeanException {
		try {
			PreparedStatement ps = DBManager.getPreparedStatement(
					"select * from `networks` where (`type`=? and `merge_scenario_id`=?) or `id`=?;");
			ps.setString(1, conet.getType().toString());
			if (conet.getMergeScenarioID() == null) {
				ps.setInt(2, Integer.MAX_VALUE);
			} else {
				ps.setInt(2, conet.getMergeScenarioID());
			}
			if (conet.getID() == null) {
				ps.setInt(3, Integer.MAX_VALUE);
			} else {
				ps.setInt(3, conet.getID());
			}
			ResultSet rs = DBManager.executeQuery(ps);
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer mergeScenarioID = rs.getInt("merge_scenario_id");
				NetworkType type = null;
				if (rs.getString("type").equals("F")) {
					type = NetworkType.FILE_BASED;
				} else if (rs.getString("type").equals("C")) {
					type = NetworkType.CHUNK_BASED;
				}
				ConflictBasedNetwork retrieved = new ConflictBasedNetwork(id,
						mergeScenarioID, type);
				return retrieved;
			}
		} catch (SQLException | ClassNotFoundException | IOException e) {
			Logger.logStackTrace(e);
		}finally{
			DBConnection.INSTANCE.closeConnection();
		}
		return null;
	}

	@Override
	public List<ConflictBasedNetwork> search(ConflictBasedNetwork object)
			throws InvalidCotonetBeanException {
		// TODO Auto-generated method stub
		return null;
	}

}