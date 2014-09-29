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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.egit.core.project.RepositoryMapping;
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
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;

public class TimeLapseView implements IShowInTarget {
	public static final String VIEWCOM_STATISTICS_OPEN = "viewcommunication/asyncEvent/statistics/open";

	public static final String MPART_ID = "eu.hohenegger.gitmine.ui.statistics";

	@Inject
	private IEclipseContext iEclipseContext;

	@Inject
	private Shell activeShell;

	@PostConstruct
	public void createPartControl(Composite shell, MPart part) {
		Scale scale = new Scale(shell, SWT.BORDER);
		Rectangle clientArea = shell.getClientArea();
		scale.setBounds(clientArea.x, clientArea.y, 200, 64);
		scale.setMaximum(40);
		scale.setPageIncrement(5);

		// Slider slider = new Slider(shell, SWT.HORIZONTAL);
		// slider.setBounds(clientArea.x + 10, clientArea.y + 10, 200, 32);
		// slider.addListener(SWT.Selection, new Listener() {
		// @Override
		// public void handleEvent(Event event) {
		// String string = "SWT.NONE";
		// switch (event.detail) {
		// case SWT.DRAG:
		// string = "SWT.DRAG";
		// break;
		// case SWT.HOME:
		// string = "SWT.HOME";
		// break;
		// case SWT.END:
		// string = "SWT.END";
		// break;
		// case SWT.ARROW_DOWN:
		// string = "SWT.ARROW_DOWN";
		// break;
		// case SWT.ARROW_UP:
		// string = "SWT.ARROW_UP";
		// break;
		// case SWT.PAGE_DOWN:
		// string = "SWT.PAGE_DOWN";
		// break;
		// case SWT.PAGE_UP:
		// string = "SWT.PAGE_UP";
		// break;
		// }
		// System.out.println("Scroll detail -> " + string);
		// }
		// });
	}

	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection) {
		if (selection == null) {
			return;
		}

		PlatformObject firstElement = (PlatformObject) selection
				.getFirstElement();


		IResource resource = (IResource) firstElement
				.getAdapter(IResource.class);

		IProject project = resource.getProject();


		RepositoryMapping mapping = RepositoryMapping.getMapping(project
				.getLocation());
		Repository repository = mapping.getRepository();

		String relativePath = createRelativePath(resource.getLocation()
				.toString(), repository);

		Git git = Git.wrap(repository);

		Iterable<RevCommit> commits;
		LogCommand logCmd = git.log().addPath(relativePath.toString());
		try {
			commits = logCmd.call();
			for (RevCommit revCommit : commits) {
				System.out.println(revCommit.getFullMessage());
			}
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
