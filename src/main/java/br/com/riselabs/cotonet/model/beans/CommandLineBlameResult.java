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

/**
 * @author Alcemir R. Santos
 *
 */
public class CommandLineBlameResult {

	private String filePath;
	private Map<Integer, String> lineAuthorsMap;
	private Map<Integer, String> lineContentMap;
	
	public CommandLineBlameResult (String aPath){
		this(aPath, new HashMap<Integer, String>(), new HashMap<Integer, String>());
	}
	
	public CommandLineBlameResult(String aPath, Map<Integer, String> aLineToAuthorMap, Map<Integer, String> aLineToContentMap) {
		this.filePath = aPath;
		this.lineAuthorsMap = aLineToAuthorMap;
		this.lineContentMap = aLineToContentMap;
	}
	
	/**
	 * may return null if the value does not exist.
	 * 
	 * @param idx
	 * @return
	 */
	public String getAuthor(int idx){
		return lineAuthorsMap.get(idx);
	}
	
	public String getFilePath(){
		return filePath;
	}

	public void addLineAuthor(Integer lineNumber, String author) {
		if (lineAuthorsMap == null) {
			lineAuthorsMap = new HashMap<Integer, String>();
		}
		lineAuthorsMap.put(lineNumber, author);
	}
	
	public void addLineContent(Integer lineNumber, String content) {
		if (lineContentMap == null) {
			lineContentMap = new HashMap<Integer, String>();
		}
		lineContentMap.put(lineNumber,content);
	}
	
	public Map<Integer, String> getLineAuthorsMap(){
		return lineAuthorsMap;
	}
	
	public List<String> getSourceContents(){
		List<String> result = new ArrayList<String>();
		result.addAll(lineContentMap.values());
		return result;
	}

	public List<String> getAuthors() {
		List<String> result = new ArrayList<String>();
		result.addAll(lineAuthorsMap.values());
		return result;
	}
	
}
