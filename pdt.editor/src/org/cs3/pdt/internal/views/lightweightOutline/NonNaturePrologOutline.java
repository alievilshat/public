/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others)
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

/*
 * Created on 31.01.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.cs3.pdt.internal.views.lightweightOutline;

import static org.cs3.prolog.common.QueryUtils.bT;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cs3.pdt.PDT;
import org.cs3.pdt.PDTPlugin;
import org.cs3.pdt.PDTPredicates;
import org.cs3.pdt.common.PDTCommonPlugin;
import org.cs3.pdt.common.PDTCommonUtil;
import org.cs3.pdt.common.PrologInterfaceStartListener;
import org.cs3.pdt.common.metadata.SourceLocation;
import org.cs3.pdt.internal.ImageRepository;
import org.cs3.pdt.internal.editors.PLEditor;
import org.cs3.pdt.internal.queries.PDTOutlineQuery;
import org.cs3.pdt.internal.structureElements.OutlineClauseElement;
import org.cs3.pdt.internal.structureElements.OutlineFileElement;
import org.cs3.pdt.internal.structureElements.OutlineModuleElement;
import org.cs3.pdt.internal.structureElements.OutlinePredicateElement;
import org.cs3.prolog.common.Util;
import org.cs3.prolog.common.logging.Debug;
import org.cs3.prolog.connector.ui.PrologRuntimeUIPlugin;
import org.cs3.prolog.pif.PrologInterface;
import org.cs3.prolog.pif.PrologInterfaceException;
import org.cs3.prolog.pif.service.ActivePrologInterfaceListener;
import org.cs3.prolog.pif.service.ConsultListener;
import org.cs3.prolog.ui.util.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;



public class NonNaturePrologOutline extends ContentOutlinePage implements ConsultListener, ActivePrologInterfaceListener, PrologInterfaceStartListener, IDoubleClickListener {
	private static final int EXPANDING_LEVEL = 2;
	public static final String MENU_ID = "org.cs3.pdt.outline.menu";
	private ITreeContentProvider contentProvider;
	private PrologSourceFileModel model;
	private PLEditor editor;
	private ILabelProvider labelProvider;
	private Menu contextMenu;
	//	private StringMatcher matcher;

	public NonNaturePrologOutline(PLEditor editor) {
		this.editor = editor;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();

		contentProvider = new OutlineContentProvider();
		viewer.setContentProvider(contentProvider);

		labelProvider = new OutlineLabelProvider();

//		labelProvider = new DecoratingLabelProvider(new OutlineLabelProvider(), 
//				PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
		viewer.setLabelProvider(labelProvider);

//		viewer.addSelectionChangedListener(this);

		viewer.addDoubleClickListener(this);
		
		viewer.getControl().addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						setInput(null);
					}
				});
			}
		});


		model = new PrologSourceFileModel(new HashMap<String,OutlineModuleElement>());

		viewer.setInput(model);

		viewer.setAutoExpandLevel(EXPANDING_LEVEL);

		IActionBars actionBars = getSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		//		Action action = new LexicalSortingAction(viewer);
		//		toolBarManager.add(action);
		Action action = new ToggleSortAction(getTreeViewer());
		toolBarManager.add(action);
		ToggleFilterAction action2 = new ToggleFilterAction(
				"Hide private predicates", 
				ImageRepository.getImageDescriptor(ImageRepository.PE_PROTECTED), 
				ImageRepository.getImageDescriptor(ImageRepository.FILTER_PRIVATE), 
				viewer, 
				new HidePrivatePredicatesFilter(), 
				PDTPlugin.getDefault().getPreferenceStore(), 
				PDT.PREF_OUTLINE_FILTER_PRIVATE);
		toolBarManager.add(action2);
		ToggleFilterAction action3 = new ToggleFilterAction(
				"Hide system predicates",
				ImageRepository.getImageDescriptor(ImageRepository.NO_FILTER_SYSTEM),
				ImageRepository.getImageDescriptor(ImageRepository.FILTER_SYSTEM),
				viewer,
				new HideSystemPredicatesFilter(),
				PDTPlugin.getDefault().getPreferenceStore(),
				PDT.PREF_OUTLINE_FILTER_SYSTEM);
		toolBarManager.add(action3);
		//		action = new FilterActionMenu(this);
		//		toolBarManager.add(action);

		hookContextMenu(parent);
		setInput(editor.getEditorInput());

		PrologRuntimeUIPlugin.getDefault().getPrologInterfaceService().registerConsultListener(this);
		PrologRuntimeUIPlugin.getDefault().getPrologInterfaceService().registerActivePrologInterfaceListener(this);
		PDTCommonPlugin.getDefault().registerPifStartListener(this);
	}


	private void fillContextMenu(IMenuManager manager) {		
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookContextMenu(Composite parent) {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				NonNaturePrologOutline.this.fillContextMenu(manager);
			}
		});
		TreeViewer viewer = getTreeViewer();
		getSite().registerContextMenu(MENU_ID,menuMgr, viewer);
		contextMenu = menuMgr.createContextMenu(parent);
		viewer.getControl().setMenu(contextMenu);
	}


	@Override
	public TreeViewer getTreeViewer() {
		return super.getTreeViewer();
	}

	public void setInput(Object information) {
		if (model == null) {
			return;
		}
		
		String fileName = editor.getPrologFileName();

		Map<String,OutlineModuleElement> modules;
		TreeViewer treeViewer = getTreeViewer();
		if (!fileName.isEmpty()) {
			Object[] expandedElements = null;
			try {			
				modules = PDTOutlineQuery.getProgramElementsForFile(fileName);
				model.update(modules, fileName);

				expandedElements = treeViewer.getExpandedElements();
				treeViewer.setInput(model);
				treeViewer.setAutoExpandLevel(EXPANDING_LEVEL);

			} catch(Exception e) {
				Debug.report(e);
			}
			if (treeViewer != null) {
				treeViewer.refresh();
				if (expandedElements != null && expandedElements.length > 0) {
					treeViewer.setExpandedElements(expandedElements);
				}
			}
		}
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		super.selectionChanged(event);
		Object elem = getFirstSelectedElement(event);
		OutlinePredicateElement predicate=null;
		String selectedFile = "";
		int line;

		if (elem == null) return;

		if (elem instanceof OutlineModuleElement) { 
			OutlineModuleElement module = (OutlineModuleElement)elem;
			line = module.getLine();
			selectedFile = module.getFilePath();
		} else if (elem instanceof OutlinePredicateElement) { 
			predicate = (OutlinePredicateElement)elem;
			line = predicate.getLine();
			selectedFile = predicate.getFileName();
		} else if (elem instanceof OutlineClauseElement) {
			OutlineClauseElement occurance = (OutlineClauseElement)elem;
			line = occurance.getLine();
			selectedFile = occurance.getFile();
//			predicate = (OutlinePredicateElement)occurance.getParent();
		} else {
			return;
		}

		String editorFileName = editor.getPrologFileName();
		if (selectedFile.equals(editorFileName)) {
			if (line > 0) {  // line = 0 means we do not have any line information
				editor.gotoLine(line);
			}
		} else {
			// FIXME: ask user if he wants to switch to the other file
			//			IFile file;
			//			try {
			//				file = FileUtils.findFileForLocation(selectedFile);
			//				SourceLocation loc = createLocation(predicate, line, file);
			//				PDTUtils.showSourceLocation(loc);
			//			} catch (IOException e) {
			//			}
		}
	}

	private Object getFirstSelectedElement(final SelectionChangedEvent event) {
		if(event.getSelection().isEmpty()){
			return null;
		}
		if(!(event.getSelection() instanceof IStructuredSelection)){
			return null;
		}
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object elem = selection.getFirstElement();

		return elem;
	}

	private SourceLocation createLocation(String functor, int arity, int line, IFile file) {
		SourceLocation loc = new SourceLocation(file.getRawLocation().toPortableString(), false);
		loc.isWorkspacePath = file.isAccessible();
		loc.setLine(line);
		loc.setPredicateName(functor);
		loc.setArity(arity);
		return loc;
	}

	@Override
	public void dispose() {
		super.dispose();
		PrologRuntimeUIPlugin.getDefault().getPrologInterfaceService().unRegisterConsultListener(this);
		PrologRuntimeUIPlugin.getDefault().getPrologInterfaceService().unRegisterActivePrologInterfaceListener(this);
		PDTCommonPlugin.getDefault().unregisterPifStartListener(this);
		contentProvider.dispose();
		model.dispose();
	}

	@Override
	public void beforeConsult(PrologInterface pif, List<IFile> files, IProgressMonitor monitor) throws PrologInterfaceException {
		monitor.beginTask("", 1);
		monitor.done();
	}

	@Override
	public void afterConsult(PrologInterface pif, List<IFile> files, List<String> allConsultedFiles, IProgressMonitor monitor) throws PrologInterfaceException {
		monitor.beginTask("", 1);

		if (pif.equals(PDTCommonUtil.getActivePrologInterface())) {
			String editorFile = editor.getPrologFileName();
			if (allConsultedFiles.contains(editorFile)) {
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						setInput(null);
					}
				});
			}
		}
		
		monitor.done();
	}

	@Override
	public void activePrologInterfaceChanged(PrologInterface pif) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
//				if (model != null && !model.hasChildren()) {
					setInput(null);
//				}
			}
		});
	}

	@Override
	public void prologInterfaceStarted(PrologInterface pif) {
		if (pif.equals(PDTCommonUtil.getActivePrologInterface())) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					setInput(null);
				}
			});
		}
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {

		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object elem = selection.getFirstElement();
		if (elem == null) return;

		String functor = null;
		int arity = 0;
		String selectedFile = "";
		int line = 0;


		if (elem instanceof OutlineModuleElement) { 
			OutlineModuleElement module = (OutlineModuleElement)elem;
			PrologInterface pif = PDTCommonUtil.getActivePrologInterface();
			if ("module".equals(module.getKind())) {
				try {
					Map<String, Object> result = pif.queryOnce(bT(PDTPredicates.MODULE_PROPERTY, Util.quoteAtom(module.getName()), "file(File)"),
							bT(PDTPredicates.MODULE_PROPERTY, Util.quoteAtom(module.getName()), "line_count(Line)"));
					if (result == null) {
						return;
					} else {
						selectedFile = result.get("File").toString();
						line = Integer.parseInt(result.get("Line").toString());
					}
				} catch (Exception e) {
					Debug.report(e);
					return;
				}
			} else { // logtalk (no modules, but entities)
				try {
					Map<String, Object> result = pif.queryOnce(bT(PDTPredicates.ENTITY_PROPERTY, Util.quoteAtom(module.getName()), "_", "file(FileName, Folder)"),
							bT(PDTPredicates.ENTITY_PROPERTY, Util.quoteAtom(module.getName()), "_", "lines(Line, _)"));
					if (result == null) {
						return;
					} else {
						selectedFile = result.get("Folder").toString() + result.get("FileName").toString();
						line = Integer.parseInt(result.get("Line").toString());
					}
				} catch (Exception e) {
					Debug.report(e);
					return;
				}
			}
		} else if (elem instanceof OutlinePredicateElement) { 
			OutlinePredicateElement predicate = (OutlinePredicateElement)elem;
			line = predicate.getLine();
			selectedFile = predicate.getFileName();
			functor = predicate.getFunctor();
			arity = predicate.getArity();
		} else if (elem instanceof OutlineClauseElement) {
			OutlineClauseElement clause = (OutlineClauseElement)elem;
			line = clause.getLine();
			selectedFile = clause.getFile();
			functor = clause.getFunctor();
			arity = clause.getArity();
		} else if (elem instanceof OutlineFileElement) {
			OutlineFileElement outlineFileElement = (OutlineFileElement) elem;
			line = outlineFileElement.getFirstLine();
			selectedFile = outlineFileElement.getFilePath();
		} else {
			return;
		}

		String editorFileName = editor.getPrologFileName();
		if (!selectedFile.equals(editorFileName)) {

			IFile file;
			try {
				file = FileUtils.findFileForLocation(selectedFile);
				SourceLocation loc = createLocation(functor, arity, line, file);
				PDTCommonUtil.showSourceLocation(loc);
			} catch (IOException e) {
			}
		}
	}
}


