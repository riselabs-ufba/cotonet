/**
 * 
 */
package br.com.riselabs.cotonet.model.beans;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author alcemirsantos
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
	
	public MergeScenario() {
		this(null, null, null, null, null);
	}

	public MergeScenario(Integer projectID, RevCommit baseCommit, RevCommit leftParent,
			RevCommit rightParent) {
		setProjectID(projectID);
		setBase(baseCommit);
		setLeft(leftParent);
		setRight(rightParent);
	}

	public MergeScenario(Integer id, Integer projectID, String sha1Base,
			String sha1Left, String sha1Right) {
		setID(id);
		setProjectID(projectID);
		setSHA1Base(sha1Base);
		setSHA1Left(sha1Left);
		setSHA1Right(sha1Right);
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
	
	

}
