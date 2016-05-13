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
package br.com.riselabs.cotonet.model.beans;

import java.util.ArrayList;
import java.util.List;

import br.com.riselabs.cotonet.model.enums.NetworkType;

/**
 * @author Alcemir R. Santos
 *
 */
public class ConflictBasedNetwork {

	private Integer id;
	private Integer mergeScenarioID;
	private MergeScenario scenario;
	private NetworkType type;
	
	private List<DeveloperNode> nodes;
	private List<DeveloperEdge> edges;
	
	/**
	 * Empty network constructor
	 */
	public ConflictBasedNetwork() {
		this(null, null, new ArrayList<DeveloperNode>(),
				new ArrayList<DeveloperEdge>(), NetworkType.CHUNK_BASED);
	}

	public ConflictBasedNetwork(Project aProject, MergeScenario aScenario) {
		this(aProject, aScenario, new ArrayList<DeveloperNode>(),
				new ArrayList<DeveloperEdge>(),NetworkType.CHUNK_BASED);
	}

	public ConflictBasedNetwork(Project aProject, MergeScenario aScenario,
			List<DeveloperNode> lNodes, List<DeveloperEdge> lEdges, NetworkType aType) {
		setScenario(aScenario);
		setNodes(lNodes);
		setEdges(lEdges);
		setType(aType);
	}
	
	public ConflictBasedNetwork(Integer id, Integer mergeScenarioID, NetworkType aType) {
		this(null, null, new ArrayList<>(), new  ArrayList<>(), aType);
		setID(id);
		setMergeScenarioID(mergeScenarioID);
	}


	public void setType(NetworkType aType) {
		this.type = aType;
	}
	
	public NetworkType getType(){
		return this.type;
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

	public MergeScenario getScenario() {
		return scenario;
	}

	public void setScenario(MergeScenario aScenario) {
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

	public void setID(Integer id){
		this.id = id;
	}
	public Integer getID() {
		return this.id;
	}
	
	public void setMergeScenarioID(Integer id){
		this.mergeScenarioID = id;
	}
	public Integer getMergeScenarioID() {
		return this.mergeScenarioID;
	}
}
