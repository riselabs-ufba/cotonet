/**
 * 
 */
package br.com.riselabs.cotonet.model.dao.validators;

import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

/**
 * @author alcemirsantos
 *
 */
public class ProjectValidator implements Validator<Project> {

	@Override
	public boolean validate(Project p) throws InvalidCotonetBeanException {
		if(p == null 
		|| p.getName() == null
		|| p.getUrl() == null){
			throw new InvalidCotonetBeanException(
					Project.class, 
					"Either the object itself, the `Name', the `URL' are <null>.",
					new NullPointerException());
		}
		
		if(p.getName().equals("")
		|| p.getUrl().equals("")){
			throw new InvalidCotonetBeanException(
					Project.class, 
					"Either the object itself, the `Name', the `URL' are empty.",
					new IllegalArgumentException());
		}
		return true;
	}

}
