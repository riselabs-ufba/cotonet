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
package br.com.riselabs.cotonet.model.beans;

import java.sql.Timestamp;

public class Commit {

	private Integer id;
	private String sha1;
	private Timestamp datetime;
	private Integer committerID;
	private Integer authorID;

	public Commit(String sha1) {
		this(null, sha1, null, null, null);
	}

	public Commit(Integer id, String sha1, Timestamp date, Integer committer,
			Integer author) {
		setID(id);
		setSHA1(sha1);
		setDatetime(date);
		setCommitterID(committer);
		setAuthorID(author);
	}

	public void setID(Integer id) {
		this.id = id;
	}

	public void setSHA1(String sha1) {
		this.sha1 = sha1;
	}

	public void setDatetime(Timestamp date) {
		this.datetime = date;
	}

	public void setCommitterID(Integer committer) {
		this.committerID = committer;
	}

	public void setAuthorID(Integer author) {
		this.authorID = author;
	}

	public String getSHA1() {
		return this.sha1;
	}

	public Timestamp getDatetime() {
		return this.datetime;
	}

	public Integer getCommitterID() {
		return this.committerID;
	}

	public Integer getAuthorID() {
		return this.authorID;
	}

	public Integer getID() {
		return this.id;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("[");
		sb.append(this.id);
		sb.append("]");
		sb.append(this.datetime);
		sb.append("-");
		sb.append(this.authorID);
		sb.append(", ");
		sb.append(this.committerID);
		return sb.toString();
	}
}
