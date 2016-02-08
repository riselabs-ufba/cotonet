/**
 * 
 */
package br.com.riselabs.crawlers.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.crawlers.test.util.GitHubDotComMock;
/**
 * @author alcemir
 *
 */
public class GitHubDotComMockTest {

	private GitHubDotComMock github;

	@Before
	public void setup() {
		github = GitHubDotComMock.getInstance();
	}

	@After
	public void teardown() {
		github.destroy();
		github = null;
	}

	@Test
	public void getRepositoryNotInitialized() {
		assertNull("Repository should be null.", github.getRepository());
	}

	@Test
	public void getRepositoryInitialized() {
		github.init();
		assertNotNull(github.getRepository());
	}

	@Test
	public void getTags() {
		fail("not implemented yet.");
	}

	@Test
	public void getCommits() {
		fail("not implemented yet.");
	}

	@Test
	public void addMergeScenarios() {
		fail("not implemented yet.");
	}

	@Test
	public void getRepossitory() {
		fail("not implemented yet.");
	}

	@Test
	public void getReposietory() {
		fail("not implemented yet.");
	}

	@Test
	public void getReposittory() {
		fail("not implemented yet.");
	}

	private static final String GERRIT_GIT_HOST = "egit.eclipse.org"; //$NON-NLS-1$

	private static final String GERRIT_PROJECT = "jgit"; //$NON-NLS-1$
	
	private Repository createRepository(String project) {
		StoredConfig config = mock(StoredConfig.class);
		Set<String> configSubSections = new HashSet<String>();
		String remoteName = "remotename"; //$NON-NLS-1$
		configSubSections.add(remoteName);
		String remoteSection = "remote"; //$NON-NLS-1$
		when(config.getSubsections(remoteSection)).thenReturn(configSubSections);
		when(config.getStringList(eq(remoteSection), eq(remoteName), anyString())).thenReturn(new String[0]);
		when(config.getStringList(eq(remoteSection), eq(remoteName), matches("url"))).thenReturn( //$NON-NLS-1$
				new String[] { "git://" + GERRIT_GIT_HOST + "/" + project }); //$NON-NLS-1$//$NON-NLS-2$
		Repository repo = mock(Repository.class);
		when(repo.getConfig()).thenReturn(config);
		return repo;
	}
}
