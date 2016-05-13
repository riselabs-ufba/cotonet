/**
 * 
 */
package br.com.riselabs.cotonet.model.dao;

import java.util.List;

import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

/**
 * @author alcemirsantos
 *
 */
public interface DAO<T> {
	public abstract boolean save(T object) throws InvalidCotonetBeanException;

	public abstract void delete(T object) throws InvalidCotonetBeanException;

	public abstract List<T> list() throws InvalidCotonetBeanException;

	public abstract T get(T object) throws InvalidCotonetBeanException;

	public abstract List<T> search(T object) throws InvalidCotonetBeanException;
}
