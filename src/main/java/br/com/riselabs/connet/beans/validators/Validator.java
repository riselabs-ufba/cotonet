/**
 * 
 */
package br.com.riselabs.connet.beans.validators;

/**
 * @author alcemirsantos
 *
 */
public interface Validator<T> {
	public boolean validate(T obj);
}
