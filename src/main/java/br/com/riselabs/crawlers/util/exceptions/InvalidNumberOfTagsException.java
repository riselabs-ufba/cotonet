package br.com.riselabs.crawlers.util.exceptions;

public class InvalidNumberOfTagsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString(){
		return  "There should exist 3 times the number of merge scenarios.";
	}
}
