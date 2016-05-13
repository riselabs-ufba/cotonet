/**
 * 
 */
package br.com.riselabs.cotonet.model.dao.validators;

import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

/**
 * @author alcemirsantos
 *
 */
public interface Validator<T> {
	public boolean validate(T obj) throws InvalidCotonetBeanException;
}
