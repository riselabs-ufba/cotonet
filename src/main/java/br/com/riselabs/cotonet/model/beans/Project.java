/**
 * 
 */
package br.com.riselabs.cotonet.model.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;

/**
 * @author alcemirsantos
 *
 */
public class Project {

	private String name;
	private String url;
	private Repository repository;
	private Map<MergeScenario, ConflictBasedNetwork> scenarioNetMap;
	private List<DeveloperNode> devs;
	private Integer id;

	public Project() {
		this(null, null, null, null);
	}
	
	public Project(String anURL){
		this(null, getRepositorySystemName(anURL), anURL, null);
	}
	
	public Project(String anURL, Repository aRepository){
		this(null, getRepositorySystemName(anURL), anURL, aRepository);
	}
	
	public Project(Integer id, String aName, String anURL, Repository aRepository) {
		setID(id);
		setName(aName);
		setUrl(anURL);
		setRepository(aRepository);
		setScenarioNetMap(new HashMap<MergeScenario, ConflictBasedNetwork>());
		setDevs(new ArrayList<DeveloperNode>());
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	/**
	 * @return the scenarioNetMap
	 */
	public Map<MergeScenario, ConflictBasedNetwork> getScenarioNetMap() {
		return scenarioNetMap;
	}

	/**
	 * @param scenarioNetMap the scenarioNetMap to set
	 */
	public void setScenarioNetMap(Map<MergeScenario, ConflictBasedNetwork> scenarioNetMap) {
		this.scenarioNetMap = scenarioNetMap;
	}
	
	public List<DeveloperNode> getDevs() {
		return devs;
	}

	public void setDevs(List<DeveloperNode> devs) {
		this.devs = devs;
	}

	public Set<MergeScenario> getMergeScenarios(){
		return this.scenarioNetMap.keySet();
	}
	
	public Iterable<ConflictBasedNetwork> getConflictBasedNetworks(){
		return this.scenarioNetMap.values();
	}
	
	/**
	 * Given a <code>Git</code> repository URL, this method returns the system.
	 * 
	 * @param remoteURL
	 * @return - system name
	 */
	private static String getRepositorySystemName(String remoteURL) {
		String[] path = remoteURL.split("/");
		return path[path.length - 1];
	}

	public void add(DeveloperNode dev) {
		if (this.devs == null) {
			this.devs = new ArrayList<DeveloperNode>();
		}
		this.devs.add(dev);
	}

	public Integer getNextID() {
		if (this.getDevs() == null) {
			this.setDevs(new ArrayList<DeveloperNode>());
			return 1;
		}else{
			return getDevs().size()+1;
		}
		
	}

	public DeveloperNode getDevByMail(String anEmail) {
		for (DeveloperNode node : getDevs()) {
			if(node.getEmail().equals(anEmail)){
				return node;
			}
		}
		return null;
	}

	public void setID(Integer anID){
		this.id = anID;
	}
	
	public Integer getID() {
		return this.id;
	}

	public void add(MergeScenario scenario, ConflictBasedNetwork connet) {
		this.scenarioNetMap.put(scenario, connet);
	}
	
}
