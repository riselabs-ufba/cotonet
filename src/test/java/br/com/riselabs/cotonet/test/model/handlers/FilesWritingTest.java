/**
 * 
 */
package br.com.riselabs.cotonet.test.model.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;
import br.com.riselabs.cotonet.util.IOHandler;
import br.com.riselabs.cotonet.util.Directories;

/**
 * @author alcemir
 *
 */
public class FilesWritingTest {

	private String testFilePath = Directories.getReposDir()+"test.txt";
	private IOHandler io;
	
	@Before
	public void setup(){
		io = new IOHandler();
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
		File f = new File(testFilePath);
		io.writeFile(f, null);
	}
	
	@Test(expected = EmptyContentException.class)
	public void tryToWriteEmptyFile() throws IOException, NullPointerException, EmptyContentException{
		File f = new File(testFilePath);
		io.writeFile(f, new ArrayList<String>());
	}
	
	@Test
	public void createdFileCorrectly() throws NullPointerException, IOException, EmptyContentException{
		List<String> l = new ArrayList<String>();
		l.add("a");
		io.writeFile(new File(testFilePath), l);
		File f = new IOHandler().getFile(new File(testFilePath));
		assertNotNull("File shouldn't be null.", f);
	}
	
	@Test
	public void wroteFileCorrectly() throws NullPointerException, IOException, EmptyContentException{
		// create content
		List<String> expectedContent = new ArrayList<String>();
		expectedContent.add("a");
		expectedContent.add("b");
		
		// write the file
		io.writeFile(new File(testFilePath), expectedContent);
		
		// read the file
		File f = new IOHandler().getFile(new File(testFilePath));
		List<String> actualContent = io.readFile(f);
		
		// check the content
		int count = 0;
		for (String actualLine : actualContent) {
			assertEquals("The line content should be the same.", expectedContent.get(count), actualLine);
			count++;
		}
		
	}
	
}
