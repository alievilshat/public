package org.cs3.pl.tuprolog.internal;

import java.util.Hashtable;

import alice.tuprolog.Library;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;

public class SyncLibrary extends Library {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 476854551154694310L;
	/**
	 * A hashtable which stores all synchornization keys.
	 */
	private Hashtable monitors = new Hashtable();

	/**
	 * 
	 * @param key key to synchronize on.
	 * @param goal
	 * @return
	 */
	public boolean with_mutex_2(Struct key, Term goal) {
		
		Object monitor = monitors.get(key.getName());

		if(monitor == null) {
			monitor = new Object();
			monitors.put(key.getName(), monitor);
		}
		synchronized (monitor) {
			SolveInfo info = this.getEngine().solve(goal);
			return (info.isSuccess())?true:false;
		}
	}
}
