/**
 * 
 */
package br.com.riselabs.connet.beans;

/**
 * @author alcemirsantos
 *
 */
public class DeveloperNode {

	private Integer id;
	private String name;
	private String email;
	
	public DeveloperNode() {
		this(null,null,null);
	}
	
	public DeveloperNode(String anEmail) {
		this(null, null, anEmail);
	}
	
	public DeveloperNode(String aName, String anEmail) {
		this(null, aName, anEmail);
	}

	public DeveloperNode(Integer anID, String aName, String anEmail) {
		setId(anID);
		setName(aName);
		setEmail(anEmail);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DeveloperNode other = (DeveloperNode) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.name + "(#" + this.id + "): " + this.email;
	}
}
