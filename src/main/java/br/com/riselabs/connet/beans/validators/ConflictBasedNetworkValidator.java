/**
 * 
 */
package br.com.riselabs.connet.beans.validators;

import br.com.riselabs.connet.beans.ConflictBasedNetwork;
import br.com.riselabs.connet.beans.DeveloperEdge;
import br.com.riselabs.connet.beans.DeveloperNode;

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
	 */
	public boolean validate(ConflictBasedNetwork obj) {
		if (obj != null) {
			this.theNetwork = obj;
		}
		if (obj.getNodes() == null || obj.getEdges() == null)
			return false;
		if (obj.getNodes().isEmpty() && !obj.getEdges().isEmpty())
			return false;
		if (!obj.getNodes().isEmpty() && obj.getEdges().isEmpty())
			return false;

		// checking nodes
		for (DeveloperNode dnode : obj.getNodes()) {
			// data must not be null
			if (dnode.getId() == null || dnode.getEmail() == null)
				return false;
			// data must not be empty
			if (dnode.getId() < 0 || dnode.getEmail().equals(""))
				return false;
			// developers must have different IDs and Emails
			for (DeveloperNode anode : obj.getNodes()) {
				if (dnode.equals(anode))
					continue;
				else if (dnode.getId() == anode.getId()
						|| dnode.getEmail().equals(anode.getEmail()))
					return false;
			}
		}
		// checking edges
		for (DeveloperEdge dedge : obj.getEdges()) {
			if (dedge.getLeft() == null || dedge.getRight() == null)
				return false;
			if (!isThereSuchDeveloper(dedge.getLeft())
					|| !isThereSuchDeveloper(dedge.getRight()))
				return false;
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
			if (n.getId() == id)
				return true;
		}
		return false;
	}
}
