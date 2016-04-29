/**
 * 
 */
package br.com.riselabs.cotonet.model.beans;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author alcemirsantos
 *
 */
public class JGitMergeScenario {

	private RevCommit base;
	private RevCommit left;
	private RevCommit right;

	public JGitMergeScenario(RevCommit baseCommit, RevCommit leftParent,
			RevCommit rightParent) {
		this.base = baseCommit;
		this.left = leftParent;
		this.right = rightParent;
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

	@Override
	public String toString() {
		return "JGitMergeScenario [base=" + base + ", left=" + left
				+ ", right=" + right + "]";
	}
	
	

}
