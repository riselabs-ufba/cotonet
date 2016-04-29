/**
 * 
 */
package br.com.riselabs.cotonet.model.dao.validators;

/**
 * @author alcemirsantos
 *
 */
public interface Validator<T> {
	public boolean validate(T obj);
}
