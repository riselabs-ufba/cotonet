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

import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

/**
 * @author Alcemir R. Santos
 *
 */
public class ConflictBasedNetworkValidator implements
		Validator<ConflictBasedNetwork> {
	private ConflictBasedNetwork theNetwork;

	/**
	 * Checks whether the network is well formed or not.
	 * 
	 * @return {@code flag} - with value <code>true</code> when the network is
	 *         well formed, <code>false</code> otherwise.
	 * @throws InvalidCotonetBeanException 
	 */
	public boolean validate(ConflictBasedNetwork obj) throws InvalidCotonetBeanException {
		if (obj != null) {
			this.theNetwork = obj;
		}
		if (obj.getNodes() == null 
				|| obj.getEdges() == null 
				|| obj.getMergeScenarioID() == null)
			throw new InvalidCotonetBeanException(
					ConflictBasedNetwork.class, 
					"Either #getNodes(), #getEdges(), or #getMergeScenarioID() returned <null>.",
					new NullPointerException());
		if (obj.getNodes().isEmpty() && !obj.getEdges().isEmpty())
			throw new InvalidCotonetBeanException(
					ConflictBasedNetwork.class, 
					"Either #getNodes() or #getEdges() were empty while the other has contents.",
					new IllegalArgumentException());
		if (!obj.getNodes().isEmpty() && obj.getEdges().isEmpty())
			throw new InvalidCotonetBeanException(
					ConflictBasedNetwork.class, 
					"Either #getNodes() or #getEdges() were empty while the other has contents.",
					new IllegalArgumentException());
		
		// checking nodes
		for (DeveloperNode dnode : obj.getNodes()) {
			// data must not be null
			if (dnode.getID() == null || dnode.getEmail() == null)
				throw new InvalidCotonetBeanException(
						ConflictBasedNetwork.class, 
						"Threre is a developer node with either an `ID' or an `Email' <null>.",
						new NullPointerException());
			// data must not be empty
			if (dnode.getID() < 0 || dnode.getEmail().equals(""))
				throw new InvalidCotonetBeanException(
						ConflictBasedNetwork.class, 
						"Threre is a developer node with either an `ID' or an `Email' invalid.",
						new IllegalArgumentException());
			// developers must have different IDs and Emails
			for (DeveloperNode anode : obj.getNodes()) {
				if (dnode.equals(anode))
					continue;
				else if (dnode.getID() == anode.getID()
						|| dnode.getEmail().equals(anode.getEmail()))
					throw new InvalidCotonetBeanException(
							ConflictBasedNetwork.class, 
							"Threre is two developer nodes with same `ID' or `Email'.",
							new NullPointerException());
			}
		}
		// checking edges
		for (DeveloperEdge dedge : obj.getEdges()) {
			if (dedge.getLeft() == null || dedge.getRight() == null)
				throw new InvalidCotonetBeanException(
						ConflictBasedNetwork.class, 
						"Threre is a developer edge with either the `LeftID' or the `RightID' <null>.",
						new NullPointerException());
			if (!isThereSuchDeveloper(dedge.getLeft())
					|| !isThereSuchDeveloper(dedge.getRight()))
				throw new InvalidCotonetBeanException(
						ConflictBasedNetwork.class, 
						"Threre is a developer edge with either an `LeftID' or an `RightID' invalid.",
						new NullPointerException());
		}
		return true;
	}

	/**
	 * checks the existence of developer with such ID.
	 * 
	 * @param id
	 * @return
	 */
	private boolean isThereSuchDeveloper(Integer id) {
		for (DeveloperNode n : this.theNetwork.getNodes()) {
			if (n.getID() == id)
				return true;
		}
		return false;
	}
}
