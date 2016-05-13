/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alcemir R. Santos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.com.riselabs.cotonet.model.dao.validators;

import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

/**
 * @author Alcemir R. Santos
 *
 */
public class DeveloperNodeValidator implements Validator<DeveloperNode> {

	@Override
	public boolean validate(DeveloperNode node) throws InvalidCotonetBeanException{
		if (node == null
				|| node.getEmail() == null
				|| node.getEmail().equals("")
				|| node.getSystemID()==null) {
			throw new InvalidCotonetBeanException(
					DeveloperNode.class, 
					"Either the object itself, the `Email', or the `SystemID' are <null>.",
					new NullPointerException());
		}
		if (node.getEmail().equals("")) {
			throw new InvalidCotonetBeanException(
					DeveloperNode.class, 
					"Developer `Email is empty.",
					new IllegalArgumentException());
		}
		ProjectDAO pdao =  (ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT);
		Project p = new Project(); 
		p.setID(node.getSystemID());
		if(pdao.get(p) == null){
			throw new InvalidCotonetBeanException(
					DeveloperNode.class, 
					"There is no such `SystemID' in the database.",
					new NullPointerException());
		}
		
		return true;
	}

}
