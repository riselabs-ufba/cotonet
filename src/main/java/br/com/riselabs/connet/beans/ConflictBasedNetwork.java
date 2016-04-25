/**
 * 
 */
package br.com.riselabs.connet.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alcemirsantos
 *
 */
public class ConflictBasedNetwork {

	private JGitMergeScenario scenario;
	private List<DeveloperNode> nodes;
	private List<DeveloperEdge> edges;

	/**
	 * Empty network constructor
	 */
	public ConflictBasedNetwork() {
		this(null, null, new ArrayList<DeveloperNode>(),
				new ArrayList<DeveloperEdge>());
	}

	public ConflictBasedNetwork(Project aProject, JGitMergeScenario aScenario) {
		this(aProject, aScenario, new ArrayList<DeveloperNode>(),
				new ArrayList<DeveloperEdge>());
	}

	public ConflictBasedNetwork(Project aProject, JGitMergeScenario aScenario,
			List<DeveloperNode> lNodes, List<DeveloperEdge> lEdges) {
		setNodes(lNodes);
		setEdges(lEdges);
	}

	/**
	 * Checks whether the network is well formed or not.
	 * 
	 * @return {@code flag} - with value <code>true</code> when the network is
	 *         well formed, <code>false</code> otherwise.
	 */
	public boolean check() {
		if (this.nodes == null || this.edges == null)
			return false;
		if (this.nodes.isEmpty() && !this.edges.isEmpty())
			return false;
		if (!this.nodes.isEmpty() && this.edges.isEmpty())
			return false;

		// checking nodes
		for (DeveloperNode dnode : nodes) {
			// data must not be null
			if (dnode.getId() == null || dnode.getEmail() == null)
				return false;
			// data must not be empty
			if (dnode.getId() < 0 || dnode.getEmail().equals(""))
				return false;
			// developers must have different IDs and Emails
			for (DeveloperNode anode : nodes) {
				if (dnode.equals(anode))
					continue;
				else if (dnode.getId() == anode.getId()
						|| dnode.getEmail().equals(anode.getEmail()))
					return false;
			}
		}
		// checking edges
		for (DeveloperEdge dedge : edges) {
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
		for (DeveloperNode n : nodes) {
			if (n.getId() == id)
				return true;
		}
		return false;
	}

	public enum NetworkType {
		CHUNK_BASED, FILE_BASED
	}

	public void setNodes(List<DeveloperNode> lNodes) {
		this.nodes = lNodes;
	}

	public void setEdges(List<DeveloperEdge> lEdges) {
		this.edges = lEdges;
	}

	public List<DeveloperNode> getNodes() {
		return this.nodes;
	}

	public List<DeveloperEdge> getEdges() {
		return this.edges;
	}

	public JGitMergeScenario getScenario() {
		return scenario;
	}

	public void setScenario(JGitMergeScenario aScenario) {
		this.scenario = aScenario;
	}

	/**
	 * This method returns a {@code DeveloperNode} if, and only if there is a
	 * match of the given name and email among the the nodes of this network. It
	 * returns {@code null}, otherwise.
	 * 
	 * @param aName
	 * @param anEmail
	 * @return a {@code DeveloperNode}
	 */
	public DeveloperNode getNode(String aName, String anEmail) {
		for (DeveloperNode node : nodes) {
			if (node.getName().equals(aName) && node.getEmail().equals(anEmail))
				return node;
		}
		return null;
	}

	public void add(DeveloperNode developerNode) {
		if (this.nodes == null) {
			this.nodes = new ArrayList<DeveloperNode>();
		}
		this.nodes.add(developerNode);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ConNet(");
		sb.append(this.scenario.toString());
		sb.append(")");
		return sb.toString();
	}

	public void add(DeveloperEdge developerEdge) {
		if (this.edges == null) {
			this.edges = new ArrayList<DeveloperEdge>();
		}
		this.edges.add(developerEdge);
	}
}
