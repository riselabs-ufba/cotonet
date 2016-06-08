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
package br.com.riselabs.cotonet.builder.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import br.com.riselabs.cotonet.model.beans.Blame;
import br.com.riselabs.cotonet.model.beans.CommandLineBlameResult;
import br.com.riselabs.cotonet.model.beans.ConflictChunk;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class ExternalGitCommand {

	public enum CommandType {
		BLAME, RESET
	}
	
	private enum PKeys{
		content,
		linenumber,
		authorname,
		authormail
	}

	private CommandType type;
	private MergeScenario scenario;
	private File file;

	public ExternalGitCommand() {
	}

	public ExternalGitCommand setMergeScenario(MergeScenario aScenario) {
		this.scenario = aScenario;
		return this;
	}
	
	public ExternalGitCommand setType(CommandType aType) {
		this.type = aType;
		return this;
	}

	public ExternalGitCommand setDirectory(File f) {
		this.file = f;
		return this;
	}

	/**
	 * OBS: this method returns {@code null} when calling '
	 * {@code git reset --hard}'.
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<ConflictChunk<CommandLineBlameResult>> call() throws IOException, RuntimeException {
		Runtime run = Runtime.getRuntime();
		Process pr;
		String cmd;
		String[] env = {};
		BufferedReader buf;
		List<ConflictChunk<CommandLineBlameResult>> conflicts = null;
		switch (type) {
		case RESET:
			cmd = "git reset --hard";
			pr = run.exec(cmd, env, file);
			break;

		case BLAME:
		default:
			cmd = "git blame -p --line-porcelain";
			env = new String[1];
			// we need this to disable the pager
			env[0] = "GIT_PAGER=cat";
			pr = run.exec(cmd + " " + file, env, file.getParentFile());
			// parse output
			buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			conflicts = new ArrayList<ConflictChunk<CommandLineBlameResult>>();

			final String CONFLICT_START = "<<<<<<<";
			final String CONFLICT_SEP = "=======";
			final String CONFLICT_END = ">>>>>>>";
			boolean addBlame = false;
			
			ConflictChunk<CommandLineBlameResult> conflict = null;
			
			CommandLineBlameResult bResult;
			bResult = new CommandLineBlameResult(file.getCanonicalPath());
			Blame<CommandLineBlameResult> cBlame;
			cBlame = new Blame<CommandLineBlameResult>(scenario.getLeft(), bResult);
			List<String> block;
			while ((block = readPorcelainBlock(buf)) != null) {
				
				Map<PKeys, String> data = getDataFromPorcelainBlock(block);

				String contentLine =  data.get(PKeys.content);
				
				if(contentLine.startsWith(CONFLICT_START)){
					addBlame =  true;
					continue;
				}else if(contentLine.startsWith(CONFLICT_SEP)) {
					addBlame = true;
					cBlame.setRevision(scenario.getLeft());
					conflict =	new ConflictChunk<CommandLineBlameResult>(file.getCanonicalPath());
					conflict.setLeft(cBlame);
					bResult =  new CommandLineBlameResult(file.getCanonicalPath());
					cBlame =  new Blame<CommandLineBlameResult>(scenario.getRight(), bResult);
					continue;
				}else if (contentLine.startsWith(CONFLICT_END)) {
					conflict.setRight(cBlame);
					conflicts.add(conflict);
					addBlame = false;
				}else if (addBlame){
					// we are in one of the conflicting chunks
					Integer linenumber = Integer.valueOf(data.get(PKeys.linenumber));
					String name = data.get(PKeys.authorname);
					String email = data.get(PKeys.authormail);
					DeveloperNode dev = new DeveloperNode(name, email);
					conflict.setLine(linenumber);
					bResult.addLineAuthor(linenumber, dev);
					bResult.addLineContent(linenumber, contentLine);
					continue;
				}
			}

			buf.close();
			break;
		}

		/*
		 * already finished to execute the process. now, we should process the
		 * error output.
		 */
		buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

		String stdErr = IOUtils.toString(pr.getErrorStream(),
				StandardCharsets.UTF_8).trim();

		IOUtils.closeQuietly(pr.getInputStream());
		IOUtils.closeQuietly(pr.getErrorStream());
		IOUtils.closeQuietly(pr.getOutputStream());

		int exitCode;
		try {
			exitCode = pr.waitFor();
		} catch (InterruptedException e) {
			exitCode = 666;
			Logger.log(String
					.format("Interrupted while waiting for '%s' to finish. Error code: '%s'",
							cmd, exitCode));
			pr.destroyForcibly();
		}

		buf.close();
		if (!stdErr.isEmpty()) {
			Logger.log(String.format(
					"Execution of '%s' returned standard error output:%n%s",
					cmd, stdErr));
			throw new RuntimeException(String.format(
					"Error on external call with exit code %d", pr.exitValue()));
		}

		return conflicts;
	}

	/**
	 * returns the the output block concerning a line blame.
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	private List<String> readPorcelainBlock(BufferedReader buf)
			throws IOException {
		List<String> block = null;
		String aLine = buf.readLine();
		if (aLine == null) {
			return null;
		}else if (aLine.split(" ")[0].length()==40) {
			block = new ArrayList<String>();
			block.add(aLine);
			while(!(aLine = buf.readLine()).startsWith("\t")){
				block.add(aLine);
			}
			block.add(aLine);
		}
		return block;
	}

	/**
	 * creates a map with the data needed from a given output block.
	 * @param filelines
	 * @return
	 */
	private Map<PKeys, String> getDataFromPorcelainBlock(List<String> filelines) {
		Map<PKeys, String> map = new HashMap<PKeys, String>();
		for (String str; (str = filelines.remove(0))!=null;) {
			if (str.trim().isEmpty() || str.startsWith("\t")) {
				map.put(PKeys.content, str.trim());
				return map;
			}
			int firstBlankSpace = str.indexOf(" ");
			String firsttoken = str.substring(0, firstBlankSpace).trim();
			String value = str.substring(firstBlankSpace).trim();
			if(firsttoken.length()==40){
				// the first line of the block
				value = str.split(" ")[2];
				map.put(PKeys.linenumber, value);
				continue;
			}else if (firsttoken.equals("author")){
				map.put(PKeys.authorname, value);
				continue;
			}else if(firsttoken.equals("author-mail")){
				value = value.substring(2, value.length()-1);
				map.put(PKeys.authormail, value);
				continue;
			}
		}
		return map;
	}
	
}
