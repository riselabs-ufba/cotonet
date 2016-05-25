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
		this.log =  log;
	}

	/**
	 * Persists a given project.
	 * 
	 * @param project
	 */
	public synchronized void persist(Project project) {
		pdao = (ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT);
		try {
			Project aux;
			if ((aux = pdao.get(project)) == null) {
				pdao.save(project);
				aux = pdao.get(project);
				current = project;
				current.setID(aux.getID());
			}
			current = project;
			current.setID(aux.getID());
			for (Entry<MergeScenario, ConflictBasedNetwork> e : project
					.getScenarioNetMap().entrySet()) {
				persistEntry(e.getKey(), e.getValue());
			}
		} catch (InvalidCotonetBeanException e) {
			Logger.logStackTrace(log, e);
		}

	}

	/**
	 * Persists the merge scenario and the conflict based network.
	 * 
	 * @param scenario
	 * @param connet
	 * @throws InvalidCotonetBeanException
	 */
	private synchronized void persistEntry(MergeScenario scenario,
			ConflictBasedNetwork connet) throws InvalidCotonetBeanException {
		// save merge scenario
		msdao = (MergeScenarioDAO) DAOFactory
				.getDAO(CotonetBean.MERGE_SCENARIO);
		scenario.setProjectID(current.getID());
		msdao.save(scenario);
		scenario.setID(msdao.get(scenario).getID());

		// save networks
		cndao = (ConflictBasedNetworkDAO) DAOFactory
				.getDAO(CotonetBean.CONFLICT_NETWORK);
		connet.setMergeScenarioID(scenario.getID());
		cndao.save(connet);
		connet.setID(cndao.get(connet).getID());

		// save edges
		dedao = (DeveloperEdgeDAO) DAOFactory.getDAO(CotonetBean.EDGE);
		for (DeveloperEdge edge : connet.getEdges()) {
			boolean leftupdated = false, rightupdated = false;
			for (Entry<Integer, DeveloperNode> node : current.getDevs().entrySet()) {
				if (leftupdated && rightupdated) {
					break;
				}
				if (node.getKey() == edge.getLeft() && !leftupdated) {
					edge.setLeft(getNodeIDFromDB(node.getValue()));
					leftupdated = true;
				} else if (node.getKey() == edge.getRight() && !rightupdated) {
					edge.setRight(getNodeIDFromDB(node.getValue()));
					rightupdated = true;
				}
			}
			edge.setNetworkID(connet.getID());
			dedao.save(edge);
		}

	}

	/**
	 * Persists the developer node if it does not exists yet and returns it ID in the database.
	 * 
	 * @param node
	 * @return
	 * @throws InvalidCotonetBeanException
	 */
	private synchronized Integer getNodeIDFromDB(DeveloperNode node)
			throws InvalidCotonetBeanException {
		// save developers
		dndao = (DeveloperNodeDAO) DAOFactory.getDAO(CotonetBean.NODE);
		DeveloperNode aux;
		if ((aux = dndao.get(node)) == null) {
			node.setSystemID(current.getID());
			dndao.save(node);
			aux = node;
		} else {
			aux = node;
		}
		return dndao.get(aux).getID();
	}

}
