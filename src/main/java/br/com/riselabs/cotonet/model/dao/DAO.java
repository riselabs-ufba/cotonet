/**
 * 
 */
package br.com.riselabs.cotonet.model.dao;

import java.util.List;

/**
 * @author alcemirsantos
 *
 */
public interface DAO<T> {
	public abstract boolean save(T object) throws IllegalArgumentException;

	public abstract void delete(T object) throws IllegalArgumentException;

	public abstract List<T> list() throws IllegalArgumentException;

	public abstract T get(T object) throws IllegalArgumentException;

	public abstract List<T> search(T object) throws IllegalArgumentException;
}
