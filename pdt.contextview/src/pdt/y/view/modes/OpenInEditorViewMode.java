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

package pdt.y.view.modes;


import java.awt.event.MouseEvent;
import java.util.Map;

import org.cs3.pdt.common.PDTCommonUtil;
import org.cs3.prolog.common.QueryUtils;
import org.cs3.prolog.pif.PrologInterfaceException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;

import pdt.y.PDTGraphPredicates;
import pdt.y.focusview.GraphPIFLoaderBase;
import pdt.y.main.PDTGraphView;
import pdt.y.model.GraphDataHolder;
import y.base.Edge;
import y.base.Node;
import y.view.EdgeLabel;
import y.view.ViewMode;

public class OpenInEditorViewMode extends ViewMode { 
	
	private PDTGraphView view;
	private GraphPIFLoaderBase pifLoader;

	public OpenInEditorViewMode(PDTGraphView view, GraphPIFLoaderBase pifLoader) {
		this.view = view;
		this.pifLoader = pifLoader;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if(event.getClickCount() >= 2) {

			// Retrieve the node that has been hit at the location.
			Node node = getHitInfo(event).getHitNode();

			if (node != null) {
				selectNode(node);
				return;
			}
			
			Edge edge = getHitInfo(event).getHitEdge();
			if (edge != null) {
				selectEdge(edge);
				return;
			}
			
			EdgeLabel label = getHitInfo(event).getHitEdgeLabel();
			if (label != null) {
				selectEdge(label.getEdge());
				return;
			}
		}
	}

	private void selectNode(Node node) {
		GraphDataHolder dataHolder = view.getDataHolder();
		if (dataHolder.isFile(node) || dataHolder.isModule(node)) {
			try {
				final String fileName = dataHolder.getFileName(node);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							PDTCommonUtil.selectInEditor(1, fileName, true);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (NullPointerException e) {}
		} else if (dataHolder.isPredicate(node)) {
			try {
				final String fileName = dataHolder.getFileName(node);
				int line = dataHolder.getLineNumber(node);
				final int lineToSelect = (line >= 1 ? line : 1);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							PDTCommonUtil.selectInEditor(lineToSelect, fileName, true);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (NullPointerException e) {}
		}
	}

	private void selectEdge(Edge edge) {
		GraphDataHolder dataHolder = view.getDataHolder();
		try {
			final String fileName = dataHolder.getFileName(edge);
			
			String offset = dataHolder.getOffset(edge);
			if (offset == null)
			{
				offset = findOffset(edge);
			}
			if (offset == null)
			{
				return;
			}
			
			String[] parts = offset.split("-");
			if (parts.length != 2)
				return;
			final int start = Integer.parseInt(parts[0]);
			final int length = Integer.parseInt(parts[1]) - start;
				
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						PDTCommonUtil.selectInEditor(start, length, fileName, true);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (NullPointerException e) {}
	}

	private String findOffset(Edge edge) {
		try {
			GraphDataHolder dataHolder = view.getDataHolder();
			Node source = edge.source();
			Node target = edge.target();
			
			String sourceModule = (String)dataHolder.getModuleOfPredicateMap().get(source);
			String sourceFunctor = (String)dataHolder.getFunctorMap().get(source);
			Integer sourceArity = (Integer)dataHolder.getArityMap().get(source);
			
			String targetModule = (String)dataHolder.getModuleOfPredicateMap().get(target);
			String targetFunctor = (String)dataHolder.getFunctorMap().get(target);
			Integer targetArity = (Integer)dataHolder.getArityMap().get(target);
			
			String LocationArgument = "TermPosition";
			Map<String, Object> res = pifLoader.sendQueryToCurrentPiF(QueryUtils.bT(PDTGraphPredicates.CALL_LOCATION_PREDICATE, sourceModule, sourceFunctor, sourceArity, targetModule, targetFunctor, targetArity, LocationArgument));
			if (res != null && res.containsKey(LocationArgument))
				return (String)res.get(LocationArgument);
			
			return null;
		
		} catch (PrologInterfaceException e) {
			e.printStackTrace();
			return null;
		}
	}
}
