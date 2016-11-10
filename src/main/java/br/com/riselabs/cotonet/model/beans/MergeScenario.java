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

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author Alcemir R. Santos
 *
 */
public class MergeScenario {

	private Integer id;
	private Integer projectID;
	private RevCommit base;
	private String SHA1Base;
	private RevCommit left;
	private String SHA1Left;
	private RevCommit right;
	private String SHA1Right;
	private RevCommit merge;
	private String SHA1Merge;
	private Timestamp mergeDate;
	
	public MergeScenario() {
		this(null, null, null, null, null, null);
	}

	public MergeScenario(RevCommit base, RevCommit left, RevCommit right, RevCommit merge, Timestamp mergeDate) {
		this(null, base, left, right, merge, mergeDate);
	}
	
	public MergeScenario(Integer projectID, RevCommit baseCommit, RevCommit leftParent,
			RevCommit rightParent, RevCommit merge, Timestamp mergeDate) {
		setProjectID(projectID);
		setBase(baseCommit);
		setLeft(leftParent);
		setRight(rightParent);
		setMerge(merge);
		setMegeDate(mergeDate);
	}

	public MergeScenario(Integer id, Integer projectID, String sha1Base,
			String sha1Left, String sha1Right, String sha1Merge, Timestamp mergeDate) {
		setID(id);
		setProjectID(projectID);
		setSHA1Base(sha1Base);
		setSHA1Left(sha1Left);
		setSHA1Right(sha1Right);
		setSHA1Merge(sha1Merge);
		setMegeDate(mergeDate);
	}

	public void setID(Integer id) {
		this.id = id;
	}
	
	public Integer getID(){
		return this.id;
	}
	
	public RevCommit getBase() {
		return base;
	}

	public void setBase(RevCommit base) {
		this.base = base;
	}

	public RevCommit getLeft() {
		return left;
	}

	public void setLeft(RevCommit left) {
		this.left = left;
	}

	public RevCommit getRight() {
		return right;
	}

	public void setRight(RevCommit right) {
		this.right = right;
	}
	
	public String getSHA1Base() {
		return SHA1Base;
	}

	public void setSHA1Base(String sHA1Base) {
		SHA1Base = sHA1Base;
	}

	public String getSHA1Left() {
		return SHA1Left;
	}

	public void setSHA1Left(String sHA1Left) {
		SHA1Left = sHA1Left;
	}

	public String getSHA1Right() {
		return SHA1Right;
	}

	public void setSHA1Right(String sHA1Right) {
		SHA1Right = sHA1Right;
	}

	public void setMerge(RevCommit aCommmit) {
		this.merge = aCommmit;
	}

	public RevCommit getMerge(){
		return this.merge;
	}

	public void setSHA1Merge(String sha1Merge) {
		this.SHA1Merge = sha1Merge;
	}
	
	public String getSHA1Merge(){
		return this.SHA1Merge;
	}

	@Override
	public String toString() {
		return "MergeScenario [base=" + base + ", left=" + left
				+ ", right=" + right + "]";
	}

	public void setProjectID(Integer projectID) {
		this.projectID = projectID;
	}
	
	public Integer getProjectID(){
		return this.projectID;
	}

	public Timestamp getMergeDate() {
		return mergeDate;
	}

	public void setMergeDate(Timestamp mergeDate) {
		this.mergeDate = mergeDate;
	}
	
	

}
