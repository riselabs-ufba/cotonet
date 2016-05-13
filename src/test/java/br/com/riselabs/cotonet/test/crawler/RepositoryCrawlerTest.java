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
package br.com.riselabs.cotonet.test.crawler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import br.com.riselabs.cotonet.crawler.RepositoryCrawler;
import br.com.riselabs.cotonet.model.exceptions.InvalidNumberOfTagsException;

/**
 * 
 * @author Alcemir R. Santos
 *
 */
public class RepositoryCrawlerTest {

	private RepositoryCrawler crawler;

	@Before
	public void setup() throws IOException{
		crawler =  new RepositoryCrawler("", false);
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
