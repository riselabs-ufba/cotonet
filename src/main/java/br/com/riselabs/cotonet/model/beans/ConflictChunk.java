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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alcemir R. Santos
 *
 */
public class ConflictChunk<T> {

	private String path;
	private Integer beginLine;
	private Integer endLine;
	
	private Blame<T> left;
	private Blame<T> right;

	public ConflictChunk(String filepath){
		setPath(filepath);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Blame<T> getRight() {
		return right;
	}

	public void setRight(Blame<T> right) {
		this.right = right;
	}

	public Blame<T> getLeft() {
		return left;
	}

	public void setLeft(Blame<T> left) {
		this.left = left;
	}

	public void setLine(Integer linenumber) {
		if (beginLine == null) {
			beginLine = linenumber;
		}
		endLine = linenumber;
	}
	
	public String getChunkRange(){
		return beginLine + "-" + endLine;
	}

	public List<Blame<T>> getBlames() {
		List<Blame<T>> blames =  new ArrayList<Blame<T>>();
		blames.add(left);
		blames.add(right);
		return blames;
	}
	

}
