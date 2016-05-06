/**
 * 
 */
package br.com.riselabs.cotonet.model.dao.validators;

import br.com.riselabs.cotonet.model.beans.Project;

/**
 * @author alcemirsantos
 *
 */
public class ProjectValidator implements Validator<Project> {

	@Override
	public boolean validate(Project p) {
		if(p == null 
		|| p.getName() == null
		|| p.getName().equals("")
		|| p.getUrl() == null
		|| p.getUrl().equals("")){
			return false;
		}
		return true;
	}

}
