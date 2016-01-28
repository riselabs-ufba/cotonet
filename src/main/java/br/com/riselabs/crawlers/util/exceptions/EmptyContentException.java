/**
 * 
 */
package br.com.riselabs.crawlers.util.exceptions;

/**
 * @author alcemir
 *
 */
public class EmptyContentException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString(){
		return "Tried to write a file without content.";
	}
}
