/**
 * 
 */
package br.com.riselabs.connet.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author alcemirsantos
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
	
	public void add(int lineNumber, String author){
		
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
