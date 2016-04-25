/**
 * 
 */
package br.com.riselabs.connet.test.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import br.com.riselabs.connet.beans.ChunkBlame;
import br.com.riselabs.connet.beans.JGitMergeScenario;
import br.com.riselabs.connet.commands.GitConflictBlame;
import br.com.riselabs.connet.test.helpers.ConflictBasedRepositoryTestCase;

/**
 * @author alcemirsantos
 *
 */
public class GitConflictBlameTest extends ConflictBasedRepositoryTestCase {

	@Test
	public void buildChunckBasedNetworkCommandLineBased() throws Exception {
		JGitMergeScenario aScenario = setCollaborationScenarioInTempRepository();
		runMerge(aScenario);
		
		String mergedfilepath = db.getDirectory().getParent().concat(File.separator+"Bar.java");
		List<ChunkBlame> chunksBlames = GitConflictBlame.getConflictingLinesBlames(new File(mergedfilepath));
		
		Iterator<ChunkBlame> iBlames = chunksBlames.iterator();
		ChunkBlame aBlame = iBlames.next();
		assertTrue(aBlame.getCommit().equals("HEAD"));
		Iterator<Entry<Integer, String>> iLines = aBlame.getResult().getLineAuthorsMap().entrySet().iterator();
		Entry<Integer, String> anEntry = iLines.next();
		assertTrue(anEntry.getKey()==3);
		assertTrue(anEntry.getValue().equals("devb@project.com"));
		anEntry = iLines.next();
		assertTrue(anEntry.getKey()==4);
		assertTrue(anEntry.getValue().equals("devc@project.com"));
		assertFalse(iLines.hasNext());
		
		aBlame = iBlames.next();
		iLines = aBlame.getResult().getLineAuthorsMap().entrySet().iterator();
		anEntry = iLines.next();
		assertTrue(anEntry.getKey()==3);
		assertTrue(anEntry.getValue().equals("deva@project.com"));
		anEntry = iLines.next();
		assertTrue(anEntry.getKey()==4);
		assertTrue(anEntry.getValue().equals("deva@project.com"));
		anEntry = iLines.next();
		assertTrue(anEntry.getKey()==5);
		assertTrue(anEntry.getValue().equals("deve@project.com"));
		assertFalse(iLines.hasNext());
		
		assertFalse(iBlames.hasNext());
	}
}
