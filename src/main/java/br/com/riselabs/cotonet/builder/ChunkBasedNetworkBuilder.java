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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.riselabs.cotonet.builder.commands.GitConflictBlame;
import br.com.riselabs.cotonet.model.beans.ChunkBlame;
import br.com.riselabs.cotonet.model.beans.CommandLineBlameResult;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;

/**
 * @author Alcemir R. Santos
 *
 */
public class ChunkBasedNetworkBuilder extends AbstractNetworkBuilder {

	/* (non-Javadoc)
	 * @see br.com.riselabs.cotonet.builder.AbstractNetworkBuilder#getDeveloperNodes(java.util.List)
	 */
	@Override
	protected List<DeveloperNode> getDeveloperNodes(MergeScenario scenario, List<File> conflictingFiles) throws IOException, InterruptedException {
		List<DeveloperNode> result = new ArrayList<DeveloperNode>();
		for (File file : conflictingFiles) {
			List<ChunkBlame> blames = GitConflictBlame
					.getConflictingLinesBlames(file);
			
			for (ChunkBlame blame : blames) {
				CommandLineBlameResult bResult = blame.getResult();
				for (String anEmail : bResult.getAuthors()) {
					DeveloperNode newNode = new DeveloperNode(anEmail);
					if (!getProject().getDevs().contains(newNode)) {
						newNode.setID(getProject().getNextID());
						getProject().add(newNode);
					} else {
						newNode = getProject().getDevByMail(anEmail);
					}
					if (!result.contains(newNode)) {
						result.add(newNode);
					}
				}
			}
		}
		return result;
	}


}
