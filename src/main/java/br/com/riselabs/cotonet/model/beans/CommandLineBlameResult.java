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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author Alcemir R. Santos
 *
 */
public class CommandLineBlameResult {

	private String filePath;
	private Map<Integer, DeveloperNode> lineAuthorsMap;
	private Map<Integer, String> lineContentMap;
	private Map<Integer, String> lineCommitMap;
	
	public CommandLineBlameResult (String aPath){
		this(aPath, new HashMap<Integer, DeveloperNode>(), new HashMap<Integer, String>(), new HashMap<>());
	}
	
	public CommandLineBlameResult(String aPath, Map<Integer, DeveloperNode> aLineToAuthorMap, Map<Integer, String> aLineToContentMap, Map<Integer, String> aLinetoCommitMap) {
		this.filePath = aPath;
		this.lineAuthorsMap = aLineToAuthorMap;
		this.lineContentMap = aLineToContentMap;
		this.lineCommitMap = aLinetoCommitMap;
	}
	
	/**
	 * may return null if the value does not exist.
	 * 
	 * @param idx
	 * @return
	 */
	public DeveloperNode getAuthor(int idx){
		return lineAuthorsMap.get(idx);
	}
	
	public String getFilePath(){
		return filePath;
	}

	public void addLineAuthor(Integer lineNumber, DeveloperNode author) {
		if (lineAuthorsMap == null) {
			lineAuthorsMap = new HashMap<Integer, DeveloperNode>();
		}
		lineAuthorsMap.put(lineNumber, author);
	}
	
	public void addLineContent(Integer lineNumber, String content) {
		if (lineContentMap == null) {
			lineContentMap = new HashMap<Integer, String>();
		}
		lineContentMap.put(lineNumber,content);
	}
	
	public void addLineCommit(Integer lineNumber, String commit) {
		if (lineCommitMap == null) {
			lineCommitMap = new HashMap<Integer, String>();
		}
		lineCommitMap.put(lineNumber,commit);
	}
	
	public Map<Integer, DeveloperNode> getLineAuthorsMap(){
		return lineAuthorsMap;
	}
	
	public Map<Integer, String> getLineCommitMap() {
		return lineCommitMap;
	}
	
	public List<String> getSourceContents(){
		List<String> result = new ArrayList<String>();
		result.addAll(lineContentMap.values());
		return result;
	}

	public List<DeveloperNode> getAuthors() {
		List<DeveloperNode> result = new ArrayList<DeveloperNode>();
		result.addAll(lineAuthorsMap.values());
		return result;
	}
}