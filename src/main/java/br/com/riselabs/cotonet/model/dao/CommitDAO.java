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
import java.util.List;

import br.com.riselabs.cotonet.model.beans.Commit;
import br.com.riselabs.cotonet.model.dao.validators.CommitValidator;
import br.com.riselabs.cotonet.model.db.Database;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class CommitDAO implements DAO<Commit> {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;
	
	private File log = null;
	
	public CommitDAO(){
	}
	
	public void setLog(File f) {
		log = f;
	}

	/**
	 * Inserts a <code>{@link Commit}</code> in the database.
	 * 
	 * @return - {@code true} whether it was possible persist the project,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean save(Commit c) throws InvalidCotonetBeanException {
		CommitValidator validator = new CommitValidator();
		boolean hasSaved = false;
		validator.validate(c);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `commits` (`sha1`, `date`, `author_id`, `committer_id`) values (?,?,?,?);");
			ps.setString(1, c.getSHA1());
			ps.setDate(2, new Date(c.getDatetime().getTime()));
			ps.setInt(3, c.getAuthorID());
			if(c.getCommitterID()!=null)
				ps.setInt(4, c.getCommitterID());
			else
				ps.setInt(4, c.getAuthorID());
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
	public void delete(Commit c) throws InvalidCotonetBeanException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"delete from `commits` whrere `id`=?;");
			ps.setInt(1, c.getID());
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
	public List<Commit> list() throws InvalidCotonetBeanException {
		// TODO implement list all commits
		return null;
	}

	@Override
	public Commit get(Commit c) throws InvalidCotonetBeanException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"select * from `commits` where `sha1`=? or `id`=?;");
			ps.setString(1, c.getSHA1());
			if (c.getID() == null) {
				ps.setInt(2, Integer.MAX_VALUE);
			} else {
				ps.setInt(2, c.getID());
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				String name = rs.getString("sha1");
				Timestamp date = new Timestamp(rs.getDate("date").getTime());
				Integer committer = rs.getInt("committer_id");
				Integer author = rs.getInt("author_id");
				Commit pResult = new Commit(id, name, date, committer, author);
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
	public List<Commit> search(Commit object)
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
