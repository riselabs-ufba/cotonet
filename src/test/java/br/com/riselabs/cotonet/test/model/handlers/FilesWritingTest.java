/**
 * 
 */
package br.com.riselabs.cotonet.test.model.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;
import br.com.riselabs.cotonet.util.Directories;
import br.com.riselabs.cotonet.util.IOHandler;

/**
 * @author alcemir
 *
 */
public class FilesWritingTest {

	private File testFile;
	private IOHandler io;
	
	@Before
	public void setup(){
		io = new IOHandler();
		testFile = new File(Directories.getReposDir()+"test.txt");
	}
	
	@After
	public void teardown(){
		if (testFile.exists()) {
			testFile.delete();
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void tryToWriteANullFile() throws IOException, NullPointerException, EmptyContentException{
		List<String> l = new ArrayList<String>();
		l.add("a");
		io.writeFile(null, l);
	}
	
	@Test(expected=Exception.class)
	public void tryToWriteADirectory() throws IOException, NullPointerException, EmptyContentException{
		io.writeFile(new File(Directories.getAppHome()), new ArrayList<String>());
	}
	
	@Test(expected = NullPointerException.class)
	public void tryToWriteNullContent() throws IOException, NullPointerException, EmptyContentException{
		io.writeFile(testFile, null);
	}
	
	@Test(expected = EmptyContentException.class)
	public void tryToWriteEmptyFile() throws IOException, NullPointerException, EmptyContentException{
		io.writeFile(testFile, new ArrayList<String>());
	}
	
	@Test
	public void createdFileCorrectly() throws NullPointerException, IOException, EmptyContentException{
		List<String> l = new ArrayList<String>();
		l.add("a");
		assertFalse(testFile.exists());
		io.writeFile(testFile, l);
		assertTrue(testFile.exists());
	}
	
	@Test
	public void wroteFileCorrectly() throws NullPointerException, IOException, EmptyContentException{
		// create content
		List<String> content = new ArrayList<String>();
		content.add("a");
		content.add("b");
		assertFalse(testFile.exists());
		
		// write the file
		io.writeFile(testFile, content);
		assertTrue(testFile.exists());
		
		// read the file
		List<String> actualContent = io.readFile(testFile);
		
		// check the content
		Iterator<String> i =  actualContent.iterator();
		String str = i.next();
		assertEquals("The line content should be the same.","a", str);
		str = i.next();
		assertEquals("The line content should be the same.","b", str);
		assertFalse(i.hasNext());
	}
	
	@Test
	public void appendLineToFileCorrectly() throws NullPointerException, IOException, EmptyContentException{
		// create content
		List<String> content = new ArrayList<String>();
		content.add("a");
		io.writeFile(testFile, content);
		assertTrue(testFile.exists());
		Iterator<String> i =  io.readFile(testFile).iterator();
		String str = i.next();
		assertEquals("The line content should be the same.","a", str);
		assertFalse(i.hasNext());
		
		// write the file
		io.appendLineToFile(testFile, "b");
		
		// read the file
		List<String> actualContent = io.readFile(testFile);
		
		// check the content
		i =  actualContent.iterator();
		str = i.next();
		assertEquals("The line content should be the same.","a", str);
		str = i.next();
		assertEquals("The line content should be the same.","b", str);
		assertFalse(i.hasNext());
	}
	
}
