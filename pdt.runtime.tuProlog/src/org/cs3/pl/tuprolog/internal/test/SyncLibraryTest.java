package org.cs3.pl.tuprolog.internal.test;

import java.util.Map;

import junit.framework.TestCase;

import org.cs3.pl.prolog.PrologException;
import org.cs3.pl.prolog.PrologInterface;
import org.cs3.pl.prolog.PrologInterfaceException;
import org.cs3.pl.prolog.PrologInterfaceFactory;
import org.cs3.pl.prolog.PrologSession;
import org.cs3.pl.tuprolog.internal.TuProlog;
import org.cs3.pl.tuprolog.internal.TuPrologPrologInterface;

import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Theory;

public class SyncLibraryTest extends TestCase {
	
	public void testSync_0() throws Exception {
		Prolog engine = new Prolog();
		engine.loadLibrary("org.cs3.pl.tuprolog.internal.SyncLibrary");
		
		//((TuPrologPrologInterface)pif).getEngine().loadLibraryClass("org.cs3.pl.tuprolog.internal.SyncLibrary");
		
		Theory sync_test = new Theory(
										"sync(test).\n" +
										"add(X):- nonvar(X),assert(X).\n" +
										"remove(X):- nonvar(X),retract(X).\n" +
										"sleep(X) :- current_thread <- sleep(X).\n" +
										"thread_name(Name) :- var(Name), current_thread <- getName returns Name.");
		//((TuPrologPrologInterface)pif).getEngine().addTheory(sync_test);
		engine.addTheory(sync_test);
		
		engine.solve("add(ab(s)).");
		SolveInfo info = engine.solve("ab(X).");
		if ( info.isSuccess() )
			assertEquals("ab(s)", info.getSolution().toString());
		
		info = engine.solve("with_mutex(ne,add(testsMutex)).");
		info = engine.solve("testsMutex.");
		assertTrue( info.isSuccess() );
		System.err.println(info.getSolution());
//		assertNotNull(session.queryOnce("sync(test)."));
	}
	
	public void testTuPrologImplementation() throws Exception {
		PrologInterface temppif = PrologInterfaceFactory.newInstance(TuProlog.FQN).create();
		
		temppif.start();
		temppif.setOption(TuPrologPrologInterface.WARNING, "true");
	
		Theory sync_test = new Theory(
										":- op( 700, xfx, '=@=').\n"+
										"'=@='(X,Y):- var(X).\n"+
										"sync(test).\n" +
										"add(X):- nonvar(X),assert(X).\n" +
										"remove(X):- nonvar(X),retract(X).\n" +
										"sleep(X) :- current_thread <- sleep(X).\n" +
										"thread_name(Name) :- var(Name), current_thread <- getName returns Name.");

		((TuPrologPrologInterface)temppif).getEngine().addTheory(sync_test);
		PrologSession tempsession = temppif.getSession();
		
		tempsession.queryOnce("add(ab(s)).");
		Map result = tempsession.queryOnce("ab(X).");
		assertEquals("s", result.get("X"));
		//assertNotNull(tempsession.queryOnce("sync(test)."));
		result = tempsession.queryOnce("A=@=B.");
		System.err.println(result.toString());
		temppif.stop();
	}

	public void testWith_mutex_2() throws Exception {

		PrologInterface temppif = PrologInterfaceFactory.newInstance(TuProlog.FQN).create();
		
		temppif.start();
		temppif.setOption(TuPrologPrologInterface.WARNING, "true");
	
		((TuPrologPrologInterface)temppif).getEngine().loadLibraryClass("org.cs3.pl.tuprolog.internal.SyncLibrary");
		
		Theory sync_test = new Theory(
										"sync(test).\n" +
										"add(X):- nonvar(X),assert(X).\n" +
										"remove(X):- nonvar(X),retract(X).\n" +
										"sleep(X) :- current_thread <- sleep(X).\n" +
										"thread_name(Name) :- var(Name), current_thread <- getName returns Name.");

		((TuPrologPrologInterface)temppif).getEngine().addTheory(sync_test);
		PrologSession session = temppif.getSession();
		
				
		Map sync = session.queryOnce("with_mutex(ne,sync(X)).");
		System.err.println(sync.toString());
		//assertEquals("X should be bind to test", "test", sync.get("X"));

		session.queryOnce("with_mutex(ne,add(jail(new_jail))).");
		sync = session.queryOnce("jail(X).");
		assertEquals("X should be bind to new_jail", "new_jail", sync.get("X"));
		
		session.queryOnce("with_mutex(ne,remove(jail(new_jail))).");
		sync = session.queryOnce("jail(X).");
		assertNull("Should be Null since there is no jail predicate.", sync);
				
	}

}
