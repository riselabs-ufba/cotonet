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
package br.com.riselabs.cotonet.test.helpers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import br.com.riselabs.cotonet.model.db.Database;

/**
 * @author Alcemir R. Santos
 *
 */
public abstract class DBTestCase {
	
	protected static final String testDBFileName = "cotonet-test-schema.sql";
	protected static SQLReader sqlReader = new SQLReader();
	protected static Connection conn;


	public static void resetTestDB() throws URISyntaxException, SQLException, ClassNotFoundException, IOException {
		conn = (conn==null)?Database.getConnection():conn;
		ClassLoader classloader = Thread.currentThread()
				.getContextClassLoader();
		File f = new File(classloader.getResource(testDBFileName).toURI());

		ArrayList<String> queries;

		sqlReader =  new SQLReader();
		queries = sqlReader.createQueries(f);

		for (String query : queries) {
			PreparedStatement pps = conn.prepareStatement(query);
			pps.execute();
		}

	}
}
