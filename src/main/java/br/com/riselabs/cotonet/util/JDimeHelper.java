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
package br.com.riselabs.cotonet.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.db.Database;

/**
 * @author Alcemir R. Santos
 *
 */
public class JDimeHelper {
	private static Connection conn;
	private static Statement st;
	private static ResultSet rs;
	
	private static final String dbname = "ghanalysis";
	private static final String dbuser = "root";
	private static final String dbpass = "root";
	
	/**
	 * Returns the merge scenarios from the 'ghanalysis' database (JDime database)
	 * 
	 * @param repositoryID
	 * @param repo
	 * @return
	 * @throws IOException
	 */
	public static List<MergeScenario> getMergeScenarios(Integer repositoryID, Repository repo) throws IOException {
		List<MergeScenario> scenarios = new ArrayList<MergeScenario>();
		String sql = "select  " + "m.leftrevision \"left\", "
				+ "m.baserevision \"base\", " + "m.rightrevision \"right\", "
				+ "m.leftcommits \"nlcommits\","
				+ "m.rightcommits \"nrcommits\"," + "l.commitid \"leftsha\","
				+ "b.commitid \"basesha\"," + "r.commitid \"rightsha\" "
				+ "from mergescenario m, revision l, revision b, revision r "
				+ "where m.leftrepo = " + repositoryID + " and m.rightrepo = "
				+ repositoryID + " and m.diffstatsdone "
				+ "and m.gitstatsdone " + "and m.commitstatsdone "
				+ "and m.leftrevision=l.id " + "and m.baserevision=b.id "
				+ "and m.rightrevision=r.id " + "and m.leftcommits > 0 "
				+ "and m.rightcommits > 0";
		// + " limit ", max_scenarios, sep="";

		try {
			conn = Database.getConnection(dbname, dbuser, dbpass);
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				try(RevWalk w = new RevWalk(repo)){
					RevCommit left = w.parseCommit(repo.resolve(rs.getString("leftsha")));
					RevCommit base = w.parseCommit(repo.resolve(rs.getString("basesha")));
					RevCommit right = w.parseCommit(repo.resolve(rs.getString("rightsha")));
					scenarios.add(new MergeScenario(repositoryID, base, left, right, null));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
		}finally{
			try {
				rs.close();
				st.close();
				conn.close();
			} catch (SQLException e) {
				Logger.logStackTrace(e);
			}
		}

		return scenarios;
	}

	
	/**
	 * Returns the URLs from the "ghanalysis" database.
	 * 
	 * @return - a map <id, url> for each entry in the "repository" table of the
	 *         database.
	 * @throws IOException 
	 */
	public Map<Integer, String> readURLsFromDatabase() throws IOException {
		Map<Integer, String> result = new HashMap<Integer, String>();
		try {
			Connection conn = Database.getConnection(dbname, dbuser, dbpass);
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select r.id ids, r.url urls from repository as r");
			while (rs.next()) {
				Integer id = rs.getInt("ids");
				String url = rs.getString("urls");
				url = url.replace("http:", "https:");
				result.put(id, url);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
		}finally{
			try {
				rs.close();
				st.close();
				conn.close();
			} catch (SQLException e) {
				Logger.logStackTrace(e);
			}
		}
		return result;
	}
}
