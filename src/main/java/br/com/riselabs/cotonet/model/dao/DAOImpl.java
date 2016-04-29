/**
 * 
 */
package br.com.riselabs.cotonet.model.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import br.com.riselabs.cotonet.model.dao.validators.Validator;
import br.com.riselabs.cotonet.model.db.DBConnection;

/**
 * @author alcemirsantos
 *
 */
public class DAOImpl<T> implements DAO<T> {

	private static Connection db ;
	private Class<T> clazz;
	private Validator<T> validator;
	
	public DAOImpl(Class<T> clazz, Validator<T> val) throws ClassNotFoundException{
		this.validator = val;
		this.clazz = clazz;
		db = DBConnection.getConnection();
	}
	
	public DAOImpl(Class<T> clazz) throws ClassNotFoundException{
		this(clazz, new DefaultValidator<T>());
	}
	
	@Override
	public synchronized boolean save(T object) {
		System.out.println("DAOImpl.save()");
		System.out.println(object);
		if(validator.validate(object)){
//			db.store(object);
//			db.commit();
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public synchronized void delete(T object) {
//		db.delete(object);
//		db.commit();
	}
	
	@Override
	public List<T> list() {
		List<T> objects = new ArrayList<T>();
//		ObjectSet<E> result = db.queryByExample(clazz);
//		while(result.hasNext()){
//			objects.add((E)result.next());
//		}
		return objects;
	}
	
	@Override
	public T get(T object) throws IllegalArgumentException{
//		List<E> objectList = db.queryByExample(clazz);
//		if(useEquals){
//			for(E each: objectList){
//				if(each.equals(object)){
//					return each;
//				}
//			}
//		}
//		else{
//			int index = objectList.indexOf(object);
//			if(index >= 0){
//				return objectList.get(index);
//			}
//		}
		throw new IllegalArgumentException("Nenhum objeto encontrado!");
	}

	@Override
	public List<T> search(T object) {
		List<T> objects = new ArrayList<T>();
//		List<E> result = db.queryByExample(object);
		return objects;		
	}

	/**
	 * Classe utilizada caso o nenhuma classe Validador seja fornecida na instanciação.
	 */
	static class DefaultValidator<T> implements Validator<T> {
		@Override
		public boolean validate(T obj) {
			return true;
		}
	}
}
