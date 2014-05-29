/*******************************************************************************
 * Copyright (c) 2012 Max Hohenegger.
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Max Hohenegger - initial implementation
 ******************************************************************************/
package eu.hohenegger.gitmine.jgit.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Miner {
	private static final String HEAD_REF = "HEAD";
	
	public Map<String, List<RevCommit>> scanAuthors(Git git) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		HashMap<String, List<RevCommit>> result = new HashMap<>();
		
		Repository repository = git.getRepository();
		RevWalk revwalk = new RevWalk(repository);
		ObjectId HEAD = repository.resolve(HEAD_REF);
		revwalk.markStart(revwalk.parseCommit(HEAD));
		Iterator<RevCommit> it = revwalk.iterator();
		while(it.hasNext()) {
			RevCommit commit = it.next();
			String authorName = commit.getAuthorIdent().getName();
			if (!result.containsKey(authorName)) {
				List<RevCommit> list = new ArrayList<>();
				list.add(commit);
				result.put(authorName, list);
			}
		}
		
		return result;
	}

}
