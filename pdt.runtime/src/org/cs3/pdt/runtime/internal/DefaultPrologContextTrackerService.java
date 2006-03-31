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

package org.cs3.pdt.runtime.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cs3.pdt.runtime.PrologContextTracker;
import org.cs3.pdt.runtime.PrologContextTrackerListener;
import org.cs3.pdt.runtime.PrologContextTrackerService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class DefaultPrologContextTrackerService implements
		PrologContextTrackerService {
	
	
	private HashMap contextTrackers=new HashMap();

	private HashMap contextTrackerListeners=new HashMap();
	

	/**
	 * register a context tracker with the runtime plugin.
	 * Any listeners that have already subscribed to the trackers id will be added.
	 * @param tracker
	 */
	public void registerPrologContextTracker(PrologContextTracker tracker) {
		contextTrackers.put(tracker.getId(),tracker);
		Set s = (Set)contextTrackerListeners.get(tracker.getId());
		if(s==null){
			s=new HashSet();
			contextTrackerListeners.put(tracker.getId(),s);
		}
		for (Iterator it = s.iterator(); it.hasNext();) {
			PrologContextTrackerListener l = (PrologContextTrackerListener) it.next();
			tracker.addPrologContextTrackerListener(l);
		}
		lateInit(tracker);
	}

	private void lateInit(final PrologContextTracker tracker) {
		if(PlatformUI.isWorkbenchRunning()){
			final IWorkbench workbench = PlatformUI.getWorkbench();
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					tracker.init(workbench);	
				}
			});
			
		}
	}

	/**
	 * unregister a context tracker from the runtime plugin.
	 * Listeners that were subscribed through the runtime plugin's global listener registry
	 * will be removed from the tracker.
	 * @param tracker
	 */
	public void unregisterPrologContextTracker(PrologContextTracker tracker) {
		contextTrackers.remove(tracker.getId());
		Set s = (Set)contextTrackerListeners.remove(tracker.getId());
		if(s==null){
			return;
		}
		
		for (Iterator it = s.iterator(); it.hasNext();) {
			PrologContextTrackerListener l = (PrologContextTrackerListener) it.next();
			tracker.removePrologContextTrackerListener(l);
		}
	}
	
	/**
	 * registers a listener for a particular tracker at the earliest possible point in time.
	 * If the tracker is already registered, the listener is added at once. Otherwise it will be 
	 * added when the tracker registeres.
	 * @param trackerID
	 * @param l
	 */
	public void addPrologContextTrackerListener(String trackerID, PrologContextTrackerListener l){
		Set s = (Set)contextTrackerListeners.get(trackerID);
		if(s==null){
			s=new HashSet();
			contextTrackerListeners.put(trackerID,s);
		}
		s.add(l);
		PrologContextTracker tracker = (PrologContextTracker) contextTrackers.get(trackerID);
		if(tracker!=null){
			tracker.addPrologContextTrackerListener(l);
		}
	}
	
	/**
	 * unregisters a listener from a particular tracker id.
	 * If the tracker is registered, the listener is removed. It is also removed from the plugins
	 * global listener table and will not be added to any other tracker registering with the same 
	 * tracker id in the future.
	 * 
	 * In othe words, this undoes the effect of addPrologContextTrackerListener(String,PrologContextListener)
	 * @param trackerID
	 * @param l
	 */
	public void removePrologContextTrackerListener(String trackerID, PrologContextTrackerListener l){
		Set s = (Set)contextTrackerListeners.get(trackerID);
		if(s==null){
			return;
		}
		s.remove(l);
		PrologContextTracker tracker = (PrologContextTracker) contextTrackers.get(trackerID);
		if(tracker!=null){
			tracker.removePrologContextTrackerListener(l);
		}
	}
	
	
	/**
	 * @return an array containing all context trackers currently registered with the runtime plugin.
	 */
	public PrologContextTracker[] getContextTrackers() {		
		return (PrologContextTracker[]) contextTrackers.values().toArray(new PrologContextTracker[contextTrackers.size()]);
	}

	public PrologContextTracker getContextTracker(String trackerId) {
		return (PrologContextTracker) contextTrackers.get(trackerId);
	}

}
