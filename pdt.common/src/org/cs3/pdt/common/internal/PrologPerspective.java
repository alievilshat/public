/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

package org.cs3.pdt.common.internal;

import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewRegistry;

public class PrologPerspective implements IPerspectiveFactory {
	
	public static final String CONSOLE_FOLDER = "prolog.perspective.console.folder";
	public static final String VIEWS_FOLDER = "prolog.perspective.views.folder";
	
	private static final String PACKAGE_EXPLORER = "org.eclipse.jdt.ui.PackageExplorer";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
		defineLayout(layout);
	}

	public void defineActions(IPageLayout layout) {
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		layout.addNewWizardShortcut("pdt.module.wizard");
	}

	public void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

//		layout.addView(JavaUI.ID_PACKAGES, IPageLayout.LEFT, (float) 0.2, editorArea);
//		layout.addView(PrologConsoleView.HOOK_ID, IPageLayout.BOTTOM, (float) 0.65, editorArea);
//		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float) 0.8, editorArea);
		
//		layout.addView(PrologConsoleView.HOOK_ID, IPageLayout.BOTTOM, (float) 0.65, editorArea);
		IFolderLayout bottomFolder = layout.createFolder(CONSOLE_FOLDER, IPageLayout.BOTTOM, 0.65f, editorArea);
		bottomFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottomFolder.addView(NewSearchUI.SEARCH_VIEW_ID);
//		layout.addView(NewSearchUI.SEARCH_VIEW_ID, IPageLayout.RIGHT, 0.5f, CONSOLE_FOLDER);
//		IFolderLayout viewsFolder =
		layout.createFolder(VIEWS_FOLDER, IPageLayout.RIGHT, 0.5f, CONSOLE_FOLDER);
		
		IViewRegistry viewRegistry = PlatformUI.getWorkbench().getViewRegistry();
		if (viewRegistry.find(PACKAGE_EXPLORER) != null) {
			layout.addView(PACKAGE_EXPLORER, IPageLayout.LEFT, 0.2f, editorArea);
		} else {
			layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.2f, editorArea);
		}
//		layout.addView(JavaUI.ID_PACKAGES, IPageLayout.LEFT, 0.2f, editorArea);
		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.8f, editorArea);
	}
	
}


