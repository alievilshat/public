/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others) 
 * E-mail: degenerl@cs.uni-bonn.de
 * WWW: http://roots.iai.uni-bonn.de/research/pdt 
 * Copyright (C): 2004-2006, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * In addition, you may at your option use, modify and redistribute any
 * part of this program under the terms of the GNU Lesser General Public
 * License (LGPL), version 2.1 or, at your option, any later version of the
 * same license, as long as
 * 
 * 1) The program part in question does not depend, either directly or
 *   indirectly, on parts of the Eclipse framework and
 *   
 * 2) the program part in question does not include files that contain or
 *   are derived from third-party work and are therefor covered by special
 *   license agreements.
 *   
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *   
 * ad 1: A program part is said to "depend, either directly or indirectly,
 *   on parts of the Eclipse framework", if it cannot be compiled or cannot
 *   be run without the help or presence of some part of the Eclipse
 *   framework. All java classes in packages containing the "pdt" package
 *   fragment in their name fall into this category.
 *   
 * ad 2: "Third-party code" means any code that was originaly written as
 *   part of a project other than the PDT. Files that contain or are based on
 *   such code contain a notice telling you so, and telling you the
 *   particular conditions under which they may be used, modified and/or
 *   distributed.
 ****************************************************************************/

package org.cs3.pl.parser.internal.term;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cs3.pl.parser.Problem;
import org.cs3.pl.parser.ProblemCollector;

public class MemberAnalyzer extends DefaultPrologTermParserVisitor {
	private ASTMember member;

	private ASTCompoundTerm neckTerm;

	private ASTCompoundTerm moduleTerm;

	protected HashSet exports = new HashSet();

	

	private boolean firstTerm = true;

	protected HashMap properties = new HashMap();

	private final ProblemCollector problemCollector;

	public Map comments = new HashMap();

	private ASTCompilationUnit compilationUnit;

	public MemberAnalyzer(ProblemCollector problemCollector) {
		this.problemCollector = problemCollector;
	}

	public Object visit(ASTCompilationUnit node, Object data) {
		this.compilationUnit = node;
		Object o = traverseChildren(node,data);
		
		
		return o;
	}
	
	public Object visit(ASTMember node, Object data) {
		this.member = node;
		this.neckTerm = null;
		this.moduleTerm = null;
		SimpleNode stopNode = (SimpleNode) super.visit(node.toCanonicalTerm(
				true, true), data);
		if (!member.isDirective()) {
			String functor = member.getHeadLiteral().getFunctor();
			String module = member.getModuleName();
			String comment = member.getComment();
			String signatur=module+":"+functor;
			if (comment != null) {
				comments.put(signatur, comment);
			}
		}

		firstTerm = false;
		return stopNode;
	}

	public Object visit(ASTCompoundTerm node, Object data) {
		String label = node.getLabel();
		int arity = node.getArity();
		if (":-".equals(label)) {
			return arity == 2 ? rule(node, data) : query(node, data);
		}
		if ("?-".equals(label)) {
			return query(node, data);
		}
		if (":".equals(label)) {
			return setModule(node, data);
		} else
			return stop(node, data);
	}

	private Object stop(SimpleNode node, Object data) {
		if (moduleTerm != null) {
			return stop(moduleTerm.getArguments()[1], null, moduleTerm
					.getArguments()[0]);
		}
		if (neckTerm != null) {
			return stop(neckTerm.getArguments()[0], neckTerm.getArguments()[1],
					null);
		}
		return stop(node, null, null);
	}

	private Object stop(SimpleNode head, SimpleNode body, SimpleNode module) {

		member.head = head == null ? null : head.getOriginal();
		member.modulePrefix = module == null ? null : module.getOriginal();
		if (body != null) {
			member.body = body.getOriginal();			
		}
		return member;
	}

	private Object setModule(ASTCompoundTerm node, Object data) {
		moduleTerm = node;
		if (neckTerm == null) {
			return node.getArguments()[1].jjtAccept(this, data);
		}
		SimpleNode head = node.getArguments()[1];
		SimpleNode body = neckTerm.getArguments()[1];
		SimpleNode module = node.getArguments()[0];
		return stop(head, body, module);
	}

	private Object query(ASTCompoundTerm node, Object data) {
		neckTerm = node;
		checkDirective(node.getArguments()[0]);
		return stop(null, node.getArguments()[0], null);
	}

	private void checkDirective(SimpleNode node) {

		if (node instanceof ASTCompoundTerm) {
			ASTCompoundTerm term = (ASTCompoundTerm) node;
			if (",".equals(term.getLabel())) {
				SimpleNode[] args = term.getArguments();
				if (args[0] instanceof ASTCompoundTerm)
					checkDirectiveHead((ASTCompoundTerm) args[0]);
				checkDirective(args[1]);
			} else {
				checkDirectiveHead(term);
			}
		}

	}

	private void checkDirectiveHead(ASTCompoundTerm node) {
		String label = node.getLabel();
		if (label.startsWith("'")) {
			label = label.substring(1, label.length() - 1);
		}
		if ("module".equals(label)) {
			checkModuleDeclaration(node);
		} else if ("dynamic".equals(label)) {
			checkPropertyDeclaration(node);
		} else if ("multifile".equals(label)) {
			checkPropertyDeclaration(node);
		}
	}

	private void checkPropertyDeclaration(ASTCompoundTerm node) {
		String property = node.getLabel();
		if (property.startsWith("'")) {
			property = property.substring(1, property.length() - 1);
		}
		SimpleNode predicatesNode = node.getArguments()[0];
		if (!(predicatesNode instanceof ASTCompoundTerm)) {
			Problem p = TermParserUtils
					.createProblem(
							predicatesNode.getOriginal(),
							"Argument should be a comma separated list of name/arity pairs.",
							Problem.ERROR);
			problemCollector.reportProblem(p);
			return;
		}
		ASTCompoundTerm predicatesCompound = (ASTCompoundTerm) predicatesNode;
		while (true) {
			if (TermParserUtils.isSignature(predicatesCompound)) {
				declareProperty(property, predicatesCompound.getOriginal());
				return;
			} else if (",".equals(predicatesCompound.getLabel())
					&&TermParserUtils.isSignature(predicatesCompound.getArguments()[0])) {
				
				declareProperty(property, predicatesCompound.getArguments()[0]
						.getOriginal());
				predicatesNode = predicatesCompound.getArguments()[1];
				if (!(predicatesNode instanceof ASTCompoundTerm)) {
					Problem p = TermParserUtils
							.createProblem(
									predicatesNode.getOriginal(),
									"Argument should be a comma separated list of name/arity pairs.",
									Problem.ERROR);
					problemCollector.reportProblem(p);
					return;
				}
				predicatesCompound = (ASTCompoundTerm) predicatesNode;
			} else {
				Problem p = TermParserUtils
						.createProblem(
								predicatesCompound.getOriginal(),
								"Argument should be a comma separated list of name/arity pairs.",
								Problem.ERROR);
				problemCollector.reportProblem(p);
				return;
			}
		}
	}

	private void declareProperty(String property, SimpleNode original) {
		List l = (List) properties.get(property);
		if (l == null) {
			l = new Vector();
			properties.put(property, l);
		}
		l.add(original);
	}

	private void checkModuleDeclaration(ASTCompoundTerm node) {
		if (!firstTerm) {

			Problem p = TermParserUtils
					.createProblem(
							node.getOriginal(),
							"Module declartions must be the first term in the input file.",
							Problem.ERROR);
			problemCollector.reportProblem(p);
			return;

		}
		SimpleNode moduleName = node.getArguments()[0];
		if(compilationUnit!=null){
			compilationUnit.moduleName=moduleName.getValue();
		}
		SimpleNode exportsNode = node.getArguments()[1];
		processExports(exportsNode);
	}

	private void processExports(SimpleNode exportsNode) {
		if (exportsNode instanceof ASTListTerm) {
			ASTListTerm listTerm = (ASTListTerm) exportsNode;
			SimpleNode[] elements = listTerm.getElements();
			//since we assume a cannonical representation, the list should be empty.
			//non empty list are represented using nested ./2 terms.
			if(elements.length!=0){
				Problem p = TermParserUtils.createProblem(
						exportsNode.getOriginal(),
						"Not your fault: not a canonical term representation: "+exportsNode.getImage(),
						Problem.ERROR);
				problemCollector.reportProblem(p);
						
			}
			return;
		}
		if (!(exportsNode instanceof ASTCompoundTerm && exportsNode.getPrincipal().getSyntheticImage()
				.equals("'.'"))) {
			Problem p = TermParserUtils.createProblem(
					exportsNode.getOriginal(),
					"The second argument term of module/2 should be a list. Found: "+exportsNode.getFunctor(),
					Problem.ERROR);
			problemCollector.reportProblem(p);
			return;
		}
		ASTCompoundTerm exportsList = (ASTCompoundTerm) exportsNode;
		
		while (exportsNode instanceof ASTCompoundTerm) {
			exportsList = (ASTCompoundTerm) exportsNode;
			processExport(exportsList.getArguments()[0]);
			exportsNode = exportsList.getArguments()[1];
		}
	}

	private void processExport(SimpleNode export) {

		if (export instanceof ASTCompoundTerm
				&& export.getFunctor().equals("op/3")) {
			exportOperator(export);

		} else if (TermParserUtils.isExportDeclaration(export)) {
			exportPredicate((ASTCompoundTerm) export);
		} else {
			Problem p = TermParserUtils.createProblem(export.getOriginal(),
					"should be '/'/2 or op/3", Problem.ERROR);
			problemCollector.reportProblem(p);
		}
	}

	private void exportOperator(SimpleNode opTerm) {
		ASTCompoundTerm t = (ASTCompoundTerm) opTerm;
		SimpleNode[] args = t.getArguments();
		Problem p = TermParserUtils.createProblem(args[0].getOriginal(),
				"The PDT is not yet able to deal with (re-)defined operators.",
				Problem.WARNING);
		problemCollector.reportProblem(p);
	}

	private void exportPredicate(ASTCompoundTerm signature) {
		SimpleNode[] args = signature.getArguments();
		if (!(args[0] instanceof ASTCharacters || args[0] instanceof ASTIdentifier)) {
			Problem p = TermParserUtils
					.createProblem(
							args[0].getOriginal(),
							"This does not look like an identifier, but i may be mistaken.",
							Problem.WARNING);

			problemCollector.reportProblem(p);
		}
		if (!(args[1] instanceof ASTInteger)) {
			Problem p = TermParserUtils.createProblem(args[0].getOriginal(),
					"The arity part of the signature should be an integer.",
					Problem.WARNING);
			problemCollector.reportProblem(p);
		}
		String label = args[0].getSyntheticImage();
		String arity = args[1].getSyntheticImage();
		exports.add(label + "/" + arity);
	}

	private Object rule(ASTCompoundTerm node, Object data) {
		neckTerm = node;
		if (moduleTerm == null) {
			return node.getArguments()[0].jjtAccept(this, data);
		}
		SimpleNode head = node.getArguments()[0];
		SimpleNode body = node.getArguments()[1];
		SimpleNode module = moduleTerm.getArguments()[0];
		return stop(head, body, module);
	}

	public Object visit(ASTCharacters node, Object data) {
		return stop(node, data);
	}

	public Object visit(ASTCut node, Object data) {
		return stop(node, data);
	}

	public Object visit(ASTFloat node, Object data) {
		return stop(node, data);
	}

	public Object visit(ASTIdentifier node, Object data) {
		return stop(node, data);
	}

	public Object visit(ASTInfixOperator node, Object data) {
		return stop(node, data);
	}

	public Object visit(ASTInteger node, Object data) {
		return stop(node, data);
	}

	public Object visit(ASTPrefixOperator node, Object data) {
		return stop(node, data);
	}

	public Object visit(ASTVariable node, Object data) {
		return stop(node, data);
	}
}
