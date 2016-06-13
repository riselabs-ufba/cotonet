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
package br.com.riselabs.cotonet.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.riselabs.cotonet.builder.commands.ExternalGitCommand;
import br.com.riselabs.cotonet.builder.commands.ExternalGitCommand.CommandType;
import br.com.riselabs.cotonet.model.beans.Blame;
import br.com.riselabs.cotonet.model.beans.CommandLineBlameResult;
import br.com.riselabs.cotonet.model.beans.ConflictChunk;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.BlameException;

/**
 * @author Alcemir R. Santos
 *
 */
public class ChunkBasedNetworkBuilder extends
		AbstractNetworkBuilder<CommandLineBlameResult> {

	public ChunkBasedNetworkBuilder(Project project) {
		setProject(project);
		setType(NetworkType.CHUNK_BASED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.com.riselabs.cotonet.builder.AbstractNetworkBuilder#getConflictChunks
	 * (br.com.riselabs.cotonet.model.beans.MergeScenario, java.io.File)
	 */
	@Override
	protected List<ConflictChunk<CommandLineBlameResult>> getConflictChunks(
			MergeScenario scenario, File file) throws BlameException {
		ExternalGitCommand egit = new ExternalGitCommand();
		List<ConflictChunk<CommandLineBlameResult>> blames = null;
		blames = egit.setMergeScenario(scenario).setDirectory(file)
					.setType(CommandType.BLAME).call();
		return blames;
	}

	/*
	 * @see
	 * br.com.riselabs.cotonet.builder.AbstractNetworkBuilder#getDeveloperNodes
	 * (java.util.List)
	 */
	@Override
	protected List<DeveloperNode> getDeveloperNodes(
			ConflictChunk<CommandLineBlameResult> cChunk) {
		List<DeveloperNode> result = new ArrayList<DeveloperNode>();

		for (Blame<CommandLineBlameResult> blame : cChunk.getBlames()) {
			CommandLineBlameResult bResult = blame.getResult();
			for (DeveloperNode aDev : bResult.getAuthors()) {
				if (!getProject().getDevs().values().contains(aDev)) {
					// if there is no such dev in the project, then add it
					getProject().add(aDev);
				} else {
					// else update the reference with the project one
					aDev = getProject().getDevByMail(aDev.getEmail());
				}
				if (!result.contains(aDev)) {
					result.add(aDev);
				}
			}
		}
		return result;
	}

}
