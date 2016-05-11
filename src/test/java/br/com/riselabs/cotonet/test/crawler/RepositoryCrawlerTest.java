package br.com.riselabs.cotonet.test.crawler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import br.com.riselabs.cotonet.crawler.ReposCrawler;
import br.com.riselabs.cotonet.model.exceptions.InvalidNumberOfTagsException;

public class RepositoryCrawlerTest {

	private ReposCrawler crawler;

	@Before
	public void setup() throws IOException{
		crawler =  new ReposCrawler( "", null);
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
		String actual =  crawler.getTupletsString(2);
		assertEquals("The tuplets were written unordered.", expected, actual);
		
	}
	
	@Test(expected = InvalidNumberOfTagsException.class)
	@Ignore
	public void getTupletsWithNoTags() throws InvalidNumberOfTagsException{
		List<String> tags=new ArrayList<String>();
		crawler.getTupletsString(tags.size());
	}
	
	@Test(expected = InvalidNumberOfTagsException.class)
	@Ignore
	public void getTupletsWithInvalidNumberOfTags() throws InvalidNumberOfTagsException{
		
		List<String> tags=new ArrayList<String>();
		tags.add("B1");
		tags.add("L1");
		tags.add("R1");
		tags.add("B2");
		tags.add("L2");

		crawler.getTupletsString(tags.size());
	}
}
