/*******************************************************************************
 * Copyright (c) 2012 Max Hohenegger.
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Max Hohenegger - initial implementation
 ******************************************************************************/
package eu.hohenegger.gitmine.jgit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.hohenegger.gitmine.IMiningService;
import eu.hohenegger.gitmine.jgit.impl.MiningService;

public class TestBasic {

	private static final String HEAD_REF = "HEAD";
	private static final String NO_EXTENSION = "";
	private static final String TMP_REPO_PREFIX = "GitRepo";
	private Git git;
	private IMiningService miner;

	@Before
	public void init() throws IOException, GitAPIException {
		File localPath = File.createTempFile(TMP_REPO_PREFIX, NO_EXTENSION);
		localPath.delete();

		git = Git.init().setDirectory(localPath).setBare(false).call();
		
		commit(new PersonIdent("alice", "alice@acme.com"));
		commit(new PersonIdent("bo", "bob@acme.com"));
		
		miner = new MiningService();
	}

	@Test
	public void testScan() throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		Map<String, List<RevCommit>> authorToCommits = miner.scanAuthors(git);
		assertTrue(!authorToCommits.isEmpty());
		assertTrue(!authorToCommits.get("alice").isEmpty());
	}
	
	public void commit(PersonIdent author) throws IOException, JGitInternalException,
			UnmergedPathsException, GitAPIException {
		String commitMessage = "Added myfile";
		
		git.commit().setMessage(commitMessage).setAuthor(author).call();
		
		Repository repository = git.getRepository();
		RevWalk revwalk = new RevWalk(repository);
		ObjectId HEAD = repository.resolve(HEAD_REF);
		revwalk.markStart(revwalk.parseCommit(HEAD));
		
		revwalk.dispose();
	}

	@After
	public void tearDown() throws Exception {
		RepositoryCache.clear();
		git.getRepository().close();
	}
}