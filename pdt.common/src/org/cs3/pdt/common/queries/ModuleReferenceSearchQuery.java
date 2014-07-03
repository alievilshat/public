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

package org.cs3.pdt.common.queries;

import static org.cs3.prolog.connector.common.QueryUtils.bT;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cs3.pdt.common.PDTCommonPredicates;
import org.cs3.pdt.common.structureElements.PrologMatch;
import org.eclipse.core.resources.IFile;

public class ModuleReferenceSearchQuery extends PDTSearchQuery {

	
	public ModuleReferenceSearchQuery(String goal, String searchGoalLabel, boolean isExactMatch) {
		super(goal, searchGoalLabel, isExactMatch);
		if (isExactMatch) {
			setSearchType("References to module");
		} else {
			setSearchType("References to modules containing");			
		}
	}

	@Override
	protected String buildSearchQuery() {
		String query = bT(PDTCommonPredicates.FIND_MODULE_REFERENCE,
				getGoal(),
				Boolean.toString(isExactMatch()),
				"RefFile",
				"RefLine",
				"RefModule",
				"RefName",
				"RefArity",
				"PropertyList");
		return query;
	}

	@Override
	protected PrologMatch constructPrologMatchForAResult(Map<String, Object> m)
	throws IOException {

		String module = m.get("RefModule").toString();
		String name = m.get("RefName").toString();
		int arity = Integer.parseInt(m.get("RefArity").toString());
		
		List<String> properties = null;
		Object prop = m.get("PropertyList");
		if (prop instanceof Vector<?>) {
			properties = (Vector<String>)prop;
		}
		IFile file = findFile(m.get("RefFile").toString());
		String offsetOrLine = m.get("RefLine").toString();
		
		PrologMatch match = null;
		
		if (offsetOrLine.indexOf("-") >= 0) {
			String[] positions = offsetOrLine.split("-");
			int offset = Integer.parseInt(positions[0]);
			int length = Integer.parseInt(positions[1]) - offset;
			match = createUniqueMatch(PROLOG_MATCH_KIND_REFERENCE, module, name, arity, file, offset, length, properties, null, "definition");
		} else {
			int line = Integer.parseInt(offsetOrLine);
			match = createUniqueMatch(PROLOG_MATCH_KIND_REFERENCE, module, name, arity, file, line, properties, null, "definition");
		}
//		int line = Integer.parseInt(m.get("RefLine").toString());
//
//		PrologMatch match = createUniqueMatch(module, name, arity, file, line, properties, null, "definition");
		return match;
	}
	
}


