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

import br.com.riselabs.cotonet.model.enums.TypeDeveloper;

/**
 * @author Alcemir R. Santos
 *
 */
public class DeveloperNode {

	private Integer id;
	private Integer systemID;
	private String name;
	private String email;
	private TypeDeveloper type;

	public DeveloperNode() {
		this(null, null, null, TypeDeveloper.AUTHOR);
	}

	public DeveloperNode(String anEmail) {
		this(null, null, anEmail, TypeDeveloper.AUTHOR);
	}

	public DeveloperNode(String aName, String anEmail) {
		this(null, aName, anEmail, TypeDeveloper.AUTHOR);
	}

	public DeveloperNode(Integer systemID, String aName, String anEmail,
			TypeDeveloper aType) {
		this(null, systemID, aName, anEmail, aType);
	}

	public DeveloperNode(Integer id, Integer systemID, String aName,
			String anEmail, TypeDeveloper aType) {
		setID(id);
		setSystemID(systemID);
		setName(aName);
		setEmail(anEmail);
		setType(aType);
	}

	public Integer getSystemID() {
		return this.systemID;
	}

	public void setSystemID(Integer systemID) {
		this.systemID = systemID;
	}

	public Integer getID() {
		return id;
	}

	public void setID(Integer id) {
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

	public void setType(TypeDeveloper aType) {
		this.type = aType;
	}

	public TypeDeveloper getType() {
		return this.type;
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
