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

import br.com.riselabs.cotonet.model.beans.Commit;
import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.ConflictBasedNetworkDAO;
import br.com.riselabs.cotonet.model.dao.DAO;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.CommitDAO;
import br.com.riselabs.cotonet.model.dao.DeveloperEdgeDAO;
import br.com.riselabs.cotonet.model.dao.DeveloperNodeDAO;
import br.com.riselabs.cotonet.model.dao.MergeScenarioDAO;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public enum DBReader {
	// Singleton implementation
	INSTANCE;

	private ProjectDAO pdao;
	private MergeScenarioDAO msdao;
	private ConflictBasedNetworkDAO cndao;
	private DeveloperEdgeDAO dedao;
	private DeveloperNodeDAO dndao;
	private CommitDAO cdao;

	private Project current;

	private File log;

	public void setLogFile(File log) {
		this.log = log;
	}

	/**
	 * Returns a project in the case it already exists in the database,
	 * <code>null</code> otherwise.
	 * 
	 * @param project
	 * @return - the project with the updated ID.
	 * @throws InvalidCotonetBeanException
	 */
	public Project exists(Project p) throws InvalidCotonetBeanException {
		pdao = (ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT);
		Project tmp = pdao.get(p);
		p.setID(tmp.getID());
		return tmp == null ? null : p;
	}
	
	public Commit exists(String sha1) throws InvalidCotonetBeanException{
		cdao = (CommitDAO) DAOFactory.getDAO(CotonetBean.COMMIT);
		Commit tmp = cdao.get(new Commit(sha1));
		return tmp == null ? null : tmp;
	}

	public Commit getCommitByID(Integer baseID) throws InvalidCotonetBeanException {
		cdao = (CommitDAO) DAOFactory.getDAO(CotonetBean.COMMIT);
		return cdao.get(new Commit(baseID, null, null, null, null));
	}

	public MergeScenario getMergeScenariobyID(Integer id) throws InvalidCotonetBeanException {
		msdao = (MergeScenarioDAO) DAOFactory.getDAO(CotonetBean.MERGE_SCENARIO);
		return msdao.get(new MergeScenario(id, null, null, null, null, null));
		
	}

	public DeveloperNode exists(DeveloperNode committer) throws InvalidCotonetBeanException {
		dndao = (DeveloperNodeDAO) DAOFactory.getDAO(CotonetBean.NODE);
		return dndao.get(committer);
	}
	
}
	