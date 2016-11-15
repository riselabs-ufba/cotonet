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

/**
 * @author Alcemir R. Santos
 *
 */
public class DeveloperEdge {
	
	private Integer id;
	private Integer networkID;

	private DeveloperNode devA;
	private DeveloperNode devB;
	
	private Integer weight;

	private String chunkRange;
	private String filepath;
	
	public DeveloperEdge() {
		this(null, null, null, null, null, null, null);
	}
	
	public DeveloperEdge(Integer id) {
		this(id, null, null, null, null, null, null);
	}

	public DeveloperEdge(DeveloperNode devA, DeveloperNode devB, Integer weightEdge, String chunkRange, String filepath) {
		this(null, null, devA, devB, weightEdge, chunkRange, filepath);
	}

	public DeveloperEdge(Integer id, Integer networkID, DeveloperNode devA, DeveloperNode devB, Integer weight, String chunkRange, String filepath) {
		setID(id);
		setNetworkID(networkID);
		setDevA(devA);
		setDevB(devB);
		setWeight(weight);
		setChunkRange(chunkRange);
		setFilepath(filepath);
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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public DeveloperNode getDevA() {
		return devA;
	}

	public void setDevA(DeveloperNode devA) {
		this.devA = devA;
	}

	public DeveloperNode getDevB() {
		return devB;
	}

	public void setDevB(DeveloperNode devB) {
		this.devB = devB;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((devA == null) ? 0 : devA.hashCode());
		result = prime * result + ((devB == null) ? 0 : devB.hashCode());
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
		if (devA == null) {
			if (other.devA != null)
				return false;
		} 
		if (devB == null) {
			if (other.devB != null)
				return false;
		}
		// testing bidirectionality
		if( (devB.equals(other.devB) && devA.equals(other.devA))
				|| (devB.equals(other.devA) && devA.equals(other.devB))){
			return true;
		}
		if((devB.equals(other.devB) && !devA.equals(other.devA))
				|| (!devB.equals(other.devB) && devA.equals(other.devA))
				|| (!devB.equals(other.devB) && !devA.equals(other.devA))){
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return "E(" + this.devA.getID() + ", " + this.devB.getID() + ")";
	}

	public void setWeight(int w){
		this.weight = w;
	}
	
	public int getWeight() {
		return this.weight;
	}

	public String getChunkRange() {
		return chunkRange;
	}

	public void setChunkRange(String chunkRange) {
		this.chunkRange = chunkRange;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
}
