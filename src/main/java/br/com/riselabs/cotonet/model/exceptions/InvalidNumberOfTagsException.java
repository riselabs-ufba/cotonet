package br.com.riselabs.cotonet.model.exceptions;

public class InvalidNumberOfTagsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int actual;

	public InvalidNumberOfTagsException(int actual){
		this.actual = actual;
	}
	@Override
	public String toString(){
		return  "There should exist 3 times the number of merge scenarios. "
				+ "The actual number was "+this.actual+".";
	}
}
