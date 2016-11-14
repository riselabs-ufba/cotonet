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
public class MergeScenario {

	private Integer id;
	private Integer projectID;
	private Integer baseID;
	private Integer leftID;
	private Integer rightID;
	private Integer mergeID;

	public MergeScenario() {
		this(null, null, null, null, null, null);
	}
	
	public MergeScenario(Integer id, Integer projectID, Integer baseID, Integer leftID,Integer rightID, Integer mergeID) {
		setID(id);
		setProjectID(projectID);
		setBaseID(baseID);
		setLeftID(leftID);
		setRightID(rightID);
		setMergeID(mergeID);
	}

	public void setID(Integer id) {
		this.id = id;
	}

	public Integer getID() {
		return this.id;
	}

	public Integer getBaseID() {
		return baseID;
	}

	public void setBaseID(Integer anID) {
		baseID = anID;
	}

	public Integer getLeftID() {
		return leftID;
	}

	public void setLeftID(Integer anID) {
		leftID = anID;
	}

	public Integer getRightID() {
		return rightID;
	}

	public void setRightID(Integer anID) {
		rightID = anID;
	}

	public void setMergeID(Integer anID) {
		this.mergeID = anID;
	}

	public Integer getMergeID() {
		return this.mergeID;
	}

	public void setProjectID(Integer projectID) {
		this.projectID = projectID;
	}

	public Integer getProjectID() {
		return this.projectID;
	}

	@Override
	public String toString() {
		return "MergeScenario [base=" + baseID + ", left=" + leftID + ", right="
				+ rightID + "]";
	}
}
