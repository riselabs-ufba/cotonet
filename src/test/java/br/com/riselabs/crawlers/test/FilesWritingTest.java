/**
 * 
 */
package br.com.riselabs.crawlers.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.core.ReposCrawler;
import br.com.riselabs.crawlers.exceptions.EmptyContentException;
import br.com.riselabs.crawlers.exceptions.InvalidNumberOfTagsException;

/**
 * @author alcemir
 *
 */
public class FilesWritingTest {

	private String testFilePath = RCProperties.getReposDir()+"test.txt";
	private IOHandler io = new IOHandler();
	
	@Test(expected=NullPointerException.class)
	public void tryToWriteANullFile() throws IOException, NullPointerException, EmptyContentException{
		List<String> l = new ArrayList<String>();
		l.add("a");
		io.writeFile(null, l);
	}
	
	@Test(expected=Exception.class)
	public void tryToWriteADirectory() throws IOException, NullPointerException, EmptyContentException{
		io.writeFile(new File(RCProperties.getUserHome()), new ArrayList<String>());
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
		List<String> actualContent = IOHandler.readFile(f);
		
		// check the content
		int count = 0;
		for (String actualLine : actualContent) {
			assertEquals("The line content should be the same.", expectedContent.get(count), actualLine);
			count++;
		}
		
	}
	
	@Test
	public void wroteTupletsCorrectly() throws InvalidNumberOfTagsException{
		
		List<String> tags=new ArrayList<String>();
		tags.add("B1");
		tags.add("L1");
		tags.add("R1");
		tags.add("B2");
		tags.add("L2");
		tags.add("R2");

		String expected =  "\"B1\", \"L1\", \"R1\", \"B2\", \"L2\", \"R2\"";
		String actual =  IOHandler.getTupletsString(tags.size());
		assertEquals("The tuplets were written unordered.", expected, actual);
		
	}
	
	@Test(expected = InvalidNumberOfTagsException.class)
	public void getTupletsWithNoTags() throws InvalidNumberOfTagsException{
		
		List<String> tags=new ArrayList<String>();
		
		IOHandler.getTupletsString(tags.size());
	}
	
	@Test(expected = InvalidNumberOfTagsException.class)
	public void getTupletsWithInvalidNumberOfTags() throws InvalidNumberOfTagsException{
		
		List<String> tags=new ArrayList<String>();
		tags.add("B1");
		tags.add("L1");
		tags.add("R1");
		tags.add("B2");
		tags.add("L2");

		IOHandler.getTupletsString(tags.size());
	}
	
	@Ignore
	@Test
	public void persistTagsMappingCorrectly() throws NullPointerException, IOException, EmptyContentException{
		fail("not implemented yet.");

		ReposCrawler c = new ReposCrawler();
		
		// TODO mock repository

		c.persistTagsMapping();
		
	}
}
