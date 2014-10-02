/*******************************************************************************
 * Copyright (c) 2012 Max Hohenegger.
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Max Hohenegger - initial implementation
 ******************************************************************************/
package eu.hohenegger.gitmine.ui.views;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.egit.ui.internal.CompareUtils;
import org.eclipse.egit.ui.internal.merge.GitCompareEditorInput;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;

public class TimeLapseView implements IShowInTarget {
	public static final String MPART_ID = "eu.hohenegger.gitmine.ui.statistics";

	private Scale scale;

	private List<RevCommit> commitList;

	private Repository repository;

	private IResource resource;

	@PostConstruct
	public void createPartControl(Composite shell, MPart part) {
		scale = new Scale(shell, SWT.BORDER);
		Rectangle clientArea = shell.getClientArea();
		scale.setBounds(clientArea.x, clientArea.y, 200, 64);

		scale.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (commitList == null || commitList.isEmpty()) {
					return;
				}
				RevCommit revCommit = commitList.get(scale.getSelection());
				RevCommit prevCommit = revCommit;
				if (scale.getSelection() > 0) {
					prevCommit = commitList.get(scale.getSelection() - 1);
				}
				execute(repository, resource, prevCommit, revCommit);
				System.out.println(revCommit.getFullMessage());
			}
		});
	}

	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection) {
		if (selection == null) {
			return;
		}

		if (!(selection.getFirstElement() instanceof PlatformObject)) {
			return;
		}

		PlatformObject firstElement = (PlatformObject) selection
				.getFirstElement();


		resource = (IResource) firstElement
				.getAdapter(IResource.class);

		IProject project = resource.getProject();


		RepositoryMapping mapping = RepositoryMapping.getMapping(project
				.getLocation());
		repository = mapping.getRepository();

		String relativePath = createRelativePath(resource.getLocation()
				.toString(), repository);

		Git git = Git.wrap(repository);

		commitList = new ArrayList<>();

		Iterable<RevCommit> commits;
		LogCommand logCmd = git.log().addPath(relativePath.toString());
		try {
			commits = logCmd.call();

			for (RevCommit revCommit : commits) {
				commitList.add(0, revCommit);
			}
			scale.setMaximum(commitList.size() - 1);
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void execute(Repository repo, Object input, RevCommit commit1,
			RevCommit commit2) {
		IWorkbenchPage workBenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (input instanceof IFile) {
			IResource[] resources = new IResource[] { (IFile) input, };
			try {
				CompareUtils.compare(resources, repo, commit1.getName(),
						commit2.getName(), false, workBenchPage);
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
		} else if (input instanceof File) {
			File fileInput = (File) input;
			IPath location = new Path(fileInput.getAbsolutePath());
			try {
				CompareUtils.compare(location, repo, commit1.getName(),
						commit2.getName(), false, workBenchPage);
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
		} else if (input instanceof IResource) {
			GitCompareEditorInput compareInput = new GitCompareEditorInput(
					commit1.name(), commit2.name(), (IResource) input);
			CompareUtils.openInCompare(workBenchPage, compareInput);
		} else if (input == null) {
			GitCompareEditorInput compareInput = new GitCompareEditorInput(
					commit1.name(), commit2.name(), repo);
			CompareUtils.openInCompare(workBenchPage, compareInput);
		}
	}

	private String createRelativePath(String absolutePath, Repository repository) {
		String relativePath;
		File directory = repository.getDirectory();
		String base = "";
		try {
			base = directory.getParentFile().getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		relativePath = createRelativePath(base, absolutePath);
		return relativePath;
	}

	public static String createRelativePath(String base, String path) {
		URI uri = new File(path).toURI();
		URI baseUri = new File(base).toURI();
		return baseUri.relativize(uri).getPath();
	}

	@Focus
	public void setFocus() {
		scale.setFocus();
	}

	@PreDestroy
	public void dispose() {
	}

	@Override
	public boolean show(ShowInContext context) {
		// TODO Auto-generated method stub
		return true;
	}
}
