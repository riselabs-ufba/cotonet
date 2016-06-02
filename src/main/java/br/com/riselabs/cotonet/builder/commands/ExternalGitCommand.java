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
import java.util.List;

import org.apache.commons.io.IOUtils;

import br.com.riselabs.cotonet.model.beans.ChunkBlame;
import br.com.riselabs.cotonet.model.beans.CommandLineBlameResult;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 *
 */
public class ExternalGitCommand {

	public enum CommandType {
		BLAME, RESET
	}

	private CommandType type;
	private File file;

	public ExternalGitCommand() {
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
	public List<ChunkBlame> call() throws IOException, RuntimeException{
		Runtime run = Runtime.getRuntime();
		Process pr;
		String cmd;
		String[] env = {};
		BufferedReader buf;
		List<ChunkBlame> blames = null;
		switch (type) {
		case RESET:
			cmd = "git reset --hard";
			pr = run.exec(cmd, env, file);
			break;

		case BLAME:
		default:
			/*
			 * -e prints the email addresses, -n the original line numbers, -f
			 * shows the name of the file.
			 */
			cmd = "git blame -f -e -n ";
			env = new String[1];
			// we need this to disable the pager
			env[0] = "GIT_PAGER=cat";
			pr = run.exec(cmd + " " + file, env, file.getParentFile());
			// parse output
			buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			blames = new ArrayList<ChunkBlame>();
			CommandLineBlameResult blameResult = new CommandLineBlameResult(
					file.getCanonicalPath());

			/*
			 * Track location by using the following encoding for the values: -1
			 * = out of conflict 0 = in variant1 1 = in variant2
			 */
			int location = -1;
			// no octopus merges supported for now ;)
			String[] revisions = new String[2];
			final String CONFLICT_START = "<<<<<<<";
			final String CONFLICT_SEP = "=======";
			final String CONFLICT_END = ">>>>>>>";
			String line;
			while ((line = buf.readLine()) != null) {
				// hack lines by blame into useful output
				line = line.trim().replaceAll("\\s+|\\t", " ");
				String author = line.substring(line.indexOf('<') + 1,
						line.indexOf('>'));
				Integer number = Integer.valueOf(line.split(" ")[2]);
				String content;
				int firstchar = line.indexOf(')') + 1;
				int lastchar = line.length();
				if (lastchar > firstchar) {
					content = line.substring(firstchar, lastchar);
				} else {
					content = "";
				}

				// control flow seems a bit odd, I did this to collect the
				// revision
				// names before
				// pushing the authors into the hashmap

				String[] markers = content.trim().split(" ", 2);
				if (markers[0].startsWith(CONFLICT_START)) {
					location = 0;
					revisions[0] = markers[1];
					continue;
				} else if (markers[0].startsWith(CONFLICT_SEP)) {
					location = 1;
				} else if (markers[0].startsWith(CONFLICT_END)) {
					revisions[1] = markers[1];
					location = -1;
				} else if (location >= 0) {
					// we are in one of the conflicting chunks
					blameResult.addLineAuthor(number, author);
					blameResult.addLineContent(number, content);
					continue;
				} else {
					continue;
				}

				// we are at separator or end of a conflict, processing
				// authors
				// found in chunk

				String revision = location == -1 ? revisions[1] : revisions[0];
				blames.add(new ChunkBlame(revision, blameResult));
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

		return blames;
	}

}
