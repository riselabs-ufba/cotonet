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
package br.com.riselabs.cotonet.model.db;

import java.io.File;
import java.util.Map.Entry;

import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.ConflictBasedNetworkDAO;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.DeveloperEdgeDAO;
import br.com.riselabs.cotonet.model.dao.DeveloperNodeDAO;
import br.com.riselabs.cotonet.model.dao.MergeScenarioDAO;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public enum DBWritter {
	// Singleton implementation
	INSTANCE;

	private ProjectDAO pdao;
	private MergeScenarioDAO msdao;
	private ConflictBasedNetworkDAO cndao;
	private DeveloperEdgeDAO dedao;
	private DeveloperNodeDAO dndao;

	private Project current;

	private File log;

	public void setLogFile(File log) {
		this.log = log;
	}

	/**
	 * Persists a given project.
	 * 
	 * @param project
	 */
	public synchronized void persist(Project project) {
		try {
			// persist the project itself
			current = persistProject(project);

			for (Entry<MergeScenario, ConflictBasedNetwork> e : project
					.getScenarioNetMap().entrySet()) {
				if(e.getValue()==null)
					continue; //ghost scenario
					
				// save merge scenario
				MergeScenario scenario = e.getKey();
				scenario.setProjectID(current.getID());
				scenario = persistScenario(scenario);

				// save networks
				ConflictBasedNetwork connet = e.getValue();
				connet.setMergeScenarioID(scenario.getID());
				connet = persistNetwork(connet);

				// save developers
				for (DeveloperNode dev : connet.getNodes()) {
					dev.setSystemID(current.getID());
					dev = persistNode(dev);
				}

				// save edges
				for (DeveloperEdge edge : connet.getEdges()) {
//					edge = updateEdgeIDs(edge, connet.getNodes());
					edge.setNetworkID(connet.getID());
					edge = persistEdge(edge);
				}
			}
		} catch (InvalidCotonetBeanException e) {
			Logger.logStackTrace(log, e);
		}
	}

	/**
	 * Persists a project in the database and update the parameter with the its
	 * respective ID.
	 * 
	 * @param project
	 * @return - the project with the updated ID.
	 * @throws InvalidCotonetBeanException
	 */
	private synchronized Project persistProject(Project project)
			throws InvalidCotonetBeanException {
		pdao = new ProjectDAO();
		Project aux;
		if ((aux = pdao.get(project)) == null) {
			pdao.save(project);
		}
		aux = pdao.get(project);
		project.setID(aux.getID());
		return project;
	}

	/**
	 * Persists a merge scenario in the database and update the parameter with
	 * the its respective ID.
	 * 
	 * @param scenario
	 * @return - the scenario with the updated ID.
	 * @throws InvalidCotonetBeanException
	 */
	private synchronized MergeScenario persistScenario(MergeScenario scenario)
			throws InvalidCotonetBeanException {
		msdao = new MergeScenarioDAO();
		MergeScenario aux;
		if ((aux = msdao.get(scenario)) == null) {
			msdao.save(scenario);
		}
		aux = msdao.get(scenario);
		scenario.setID(aux.getID());
		return scenario;
	}

	/**
	 * Persists a network in the database and update the parameter with the its
	 * respective ID.
	 * 
	 * @param network
	 * @return - the network with the updated ID.
	 * @throws InvalidCotonetBeanException
	 */
	private synchronized ConflictBasedNetwork persistNetwork(
			ConflictBasedNetwork connet) throws InvalidCotonetBeanException {
		cndao = new ConflictBasedNetworkDAO();
		ConflictBasedNetwork aux;
		if ((aux = cndao.get(connet)) == null) {
			cndao.save(connet);
		}
		aux = cndao.get(connet);
		connet.setID(aux.getID());
		return connet;
	}

	/**
	 * Persists an edge in the database and update the parameter with the its
	 * respective ID.
	 * 
	 * @param edge
	 * @return - the edge with the updated ID.
	 * @throws InvalidCotonetBeanException
	 */
	private synchronized DeveloperEdge persistEdge(DeveloperEdge edge)
			throws InvalidCotonetBeanException {
		dedao = new DeveloperEdgeDAO();
		DeveloperEdge aux;
		if ((aux = dedao.get(edge)) == null) {
			dedao.save(edge);
		}
		aux = dedao.get(edge);
		edge.setID(aux.getID());
		return edge;
	}

	/**
	 * Persists a node in the database and update the parameter with the its
	 * respective ID.
	 * 
	 * @param edge
	 * @return - the edge with the updated ID.
	 * @throws InvalidCotonetBeanException
	 */
	private synchronized DeveloperNode persistNode(DeveloperNode node)
			throws InvalidCotonetBeanException {
		// save developers
		dndao = (DeveloperNodeDAO) DAOFactory.getDAO(CotonetBean.NODE);
		DeveloperNode aux;
		if ((aux = dndao.get(node)) == null) {
			dndao.save(node);
		}
		aux = dndao.get(node);
		node.setID(aux.getID());
		return node;
	}
}
