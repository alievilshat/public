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

package org.cs3.pl.metadata;

import java.io.Serializable;
import java.util.Comparator;


/**
 * a tuple describing a logical prolog element like a predicate or a clause.

 */
public class PrologElement implements Serializable, Comparable<PrologElement>{

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	protected String contextModule;
	protected String functorName;
	protected int arity;

	protected boolean dynamic;
	protected boolean multifile;
	protected boolean pub;

	protected boolean isKnown;

	
	/**
	 * Creates a PrologElementData Entity. This class is a container for
	 * Prolog elements like facts, clauses or modules. 
	 * 
	 * @param functorName
	 * @param arity if arity is -1 the element is a module.
	 */
	protected PrologElement(String contextModule,String functorName, int arity,boolean pub, boolean dynamic, boolean multifile) {
		this.pub = pub;
		this.functorName = functorName;
		this.arity = arity;
		this.dynamic = dynamic;
		this.multifile =multifile;
		this.contextModule=contextModule;		
		this.isKnown=true;
	}

	/**
	 * Creates a PrologElementData Entity. This class is a container for
	 * Prolog elements like facts, clauses or modules. 
	 * 
	 * @param functorName
	 * @param arity if arity is -1 the element is a module.
	 */
	protected PrologElement(String contextModule,String functorName, int arity) {
		this.contextModule=contextModule;
		this.functorName = functorName;
		this.arity = arity;
		this.isKnown=false;
	}

	/**
	 * Returns the signature of the predicate:
	 * module:name/arity.
	 * 
	 * @return
	 */
	public String getSignature() {
//		if(arity == -1)
//			return functorName + " (module)";
		String modulePart;
		if(contextModule == null)
			modulePart = "";
		else 
			modulePart = contextModule + ":";
		return modulePart + functorName + "/" + arity;
	}

	@Override
	public String toString() {
		return getSignature();
	}

	public String getModule() {
		return contextModule;
	}

	public String getName() {
		return functorName;
	}
	
	public int getArity() {
		return arity;
	}

	/**
	 * @return Returns the pub.
	 */
	public boolean isPublic() {
		if(!isKnown){
			throw new UnsupportedOperationException("Not enough information.");
		}
		return pub;
	}

	public boolean isDynamic() {
		if(!isKnown){
			throw new UnsupportedOperationException("Not enough information.");
		}
		return dynamic;
	}

	public boolean isMultifile() {
		if(!isKnown){
			throw new UnsupportedOperationException("Not enough information.");
		}
		return multifile;
	}

	public boolean isKnown() {
		return isKnown;
	}

	@Override
	public int hashCode() {
		return getSignature().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if(obj instanceof PrologElement){
			return ((PrologElement)obj).getSignature().equals(getSignature());
		}
		return super.equals(obj);
	}

	static public Comparator<PrologElement> getComparator() {
		return new Comparator<PrologElement>() {
			@Override
			public int compare(PrologElement arg0, PrologElement arg1) {
				return arg0.getSignature().compareTo(
						arg1.getSignature());
			}
		};
	}

	@Override
	public int compareTo(PrologElement arg0) {
		return getSignature().compareTo(((Predicate)arg0).getSignature());
	}

}
