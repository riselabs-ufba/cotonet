/**
 * 
 */
package br.com.riselabs.cotonet.model.beans;

/**
 * @author alcemirsantos
 *
 */
public class DeveloperEdge {
	
	private Integer id;
	private Integer networkID;

	private Integer left;
	private Integer right;
	private Integer weight;

	public DeveloperEdge() {
		this(null, null, null, null);
	}
	
	public DeveloperEdge(Integer id) {
		this(id, null, null, null);
	}
	
	public DeveloperEdge(Integer left, Integer right) {
		this(null, null, left, right);
	}

	public DeveloperEdge(Integer id, Integer networkID, Integer left, Integer right) {
		setID(id);
		setNetworkID(networkID);
		setLeft(left);
		setRight(right);
		setWeight(1);
	}
	
	public Integer getID() {
		return this.id;
	}

	public void setID(Integer left) {
		this.id = left;
	}
	
	public Integer getNetworkID() {
		return this.networkID;
	}

	public void setNetworkID(Integer left) {
		this.networkID = left;
	}

	public Integer getLeft() {
		return left;
	}

	public void setLeft(Integer left) {
		this.left = left;
	}

	public Integer getRight() {
		return right;
	}

	public void setRight(Integer right) {
		this.right = right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeveloperEdge other = (DeveloperEdge) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} 
		if (right == null) {
			if (other.right != null)
				return false;
		}
		// testing bidirectionality
		if( (right.equals(other.right) && left.equals(other.left))
				|| (right.equals(other.left) && left.equals(other.right))){
			return true;
		}
		if((right.equals(other.right) && !left.equals(other.left))
				|| (!right.equals(other.right) && left.equals(other.left))
				|| (!right.equals(other.right) && !left.equals(other.left))){
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return "Edge(" + this.left + ", " + this.right + ")";
	}

	public void setWeight(int w){
		this.weight = w;
	}
	
	public int getWeight() {
		return this.weight;
	}
}
