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

/**
 * a tupel describing a position within a sourcefile. <br>
 * This is just used as a means of using structured data as argument and return
 * value. <br>
 * Rows are counted starting from 1. <br>
 * Offsets are counted starting from 0. <br>
 * Last rows are always inclusive. <br>
 * End offsets are always exclusive.
 * 
 * Two instances of this class are considered equal, if 
 *  - they are both either streambased or line base,
 *  - they both are refer to a workspace or an absolute file
 *  - they both describe exactly the same "physical" character sequence.
 *  
 * 
 */
public final class SourceLocation implements Serializable, Comparable<SourceLocation> {
	private static final long serialVersionUID = 1L;

	/**
	 * The offset within the source file / source line. <br>
	 * If this is a line-based source location, this is the character position
	 * within the containing line. The first character in a line is character 0.
	 * <br>
	 * If this location is NOT row-based, this is the offset within the
	 * containing character stream. The first character in the file is at offset
	 * 0.
	 */
	public int offset = 0;

	/**
	 * The end offset of a range. (exclusive) <br>
	 * For line-based locations, this is the position of the first character
	 * that does NOT belong to the range within the last line that DOES belong
	 * to the range. <br>
	 * For stream-based locations, this is the offset of the first character
	 * that does NOT belong to the range.
	 */
	public int endOffset = 0;

	/**
	 * The absolute path to the file containing this location. <br>
	 * The should be interpreted as workspace path or as filesystem path,
	 * depending on the value of isWorkspacePath.
	 */
	public String file;

	/**
	 * Determines wether the path is workspace-relative.
	 */
	public boolean isWorkspacePath;

	public SourceLocation(String file, boolean isWorkspacePath) {
		this.file = file;
		this.isWorkspacePath = isWorkspacePath;
	}
	
	@Override
	public String toString() {
		return file + "/" + offset;
	}

	@Override
	public int compareTo(SourceLocation arg0) {
		SourceLocation other = arg0;
		// workspace paths come before absolute paths.
		int c = (isWorkspacePath ? 0 : 1) - (other.isWorkspacePath ? 0 : 1);
		if (c != 0) {
			return c;
		}
		// lexicographic comparision of files
		c = file.compareTo(other.file);
		if (c != 0) {
			return c;
		}
		// if there is still no difference, both positions are
		// either row based or stream based.
		c = offset-other.offset ;
		if (c != 0) {
			return c;
		}
		c = endOffset-other.endOffset;
		if (c != 0) {
			return c;
		}
		//both source locations describe the same "physical" character sequence.
		return 0;
	}

	@Override
	public boolean equals(Object obj) {		
		if(obj instanceof SourceLocation) {
			return compareTo((SourceLocation)obj)==0;
		}
		else return false;
	}
	
	@Override
	public int hashCode() {
		return file.hashCode()+offset+endOffset;
	}
}
