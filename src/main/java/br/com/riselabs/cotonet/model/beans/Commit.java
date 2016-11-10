package br.com.riselabs.cotonet.model.beans;

import java.sql.Timestamp;

public class Commit {

	private Integer id;
	private String sha1;
	private Timestamp datetime;
	private Integer committerID;
	private Integer authorID;

	public Commit(String sha1) {
		this(sha1, null, null, null);
	}

	public Commit(String sha1, Timestamp date, Integer committer, Integer author) {
		this(null, sha1, date, committer, author);
	}

	public Commit(Integer id, String sha1, Timestamp date, Integer committer,
			Integer author) {
		setID(id);
		setHash(sha1);
		setDatetime(date);
		setCommitterID(committer);
		setAuthorID(author);
	}

	private void setID(Integer id) {
		this.id = id;
	}

	private void setHash(String sha1) {
		this.sha1 = sha1;
	}

	private void setDatetime(Timestamp date) {
		this.datetime = date;
	}

	private void setCommitterID(Integer committer) {
		this.committerID = committer;
	}

	private void setAuthorID(Integer author) {
		this.authorID = author;
	}

	public String getHash() {
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

}
