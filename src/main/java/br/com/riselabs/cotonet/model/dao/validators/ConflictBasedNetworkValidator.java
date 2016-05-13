/**
 * 
 */
package br.com.riselabs.cotonet.model.dao.validators;

import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

/**
 * @author alcemirsantos
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
