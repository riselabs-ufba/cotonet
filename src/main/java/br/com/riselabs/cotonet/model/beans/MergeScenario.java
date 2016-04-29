/**
 * 
 */
package br.com.riselabs.cotonet.model.beans;

/**
 * @author Alcemir R. Santos
 *
 */
public class MergeScenario {
	private String base;
	private String left;
	private String right;
	private Integer numCommitsLeft;
	private Integer numCommitsRight;
	
	public MergeScenario(String base, String left, String right, Integer numCommitsLeft, Integer numCommitsRight){
		this.base = base;
		this.left = left;
		this.right = right;
		this.numCommitsLeft = numCommitsLeft;
		this.numCommitsRight = numCommitsRight;
	}
	
	public String getBase() {
		return base;
	}
	public void setBase(String base) {
		this.base = base;
	}
	public String getLeft() {
		return left;
	}
	public void setLeft(String left) {
		this.left = left;
	}
	public String getRight() {
		return right;
	}
	public void setRight(String right) {
		this.right = right;
	}
	public Integer getNumCommitsLeft() {
		return numCommitsLeft;
	}
	public void setNumCommitsLeft(Integer numCommitsLeft) {
		this.numCommitsLeft = numCommitsLeft;
	}
	public Integer getNumCommitsRight() {
		return numCommitsRight;
	}
	public void setNumCommitsRight(Integer numCommitsRight) {
		this.numCommitsRight = numCommitsRight;
	}
}
