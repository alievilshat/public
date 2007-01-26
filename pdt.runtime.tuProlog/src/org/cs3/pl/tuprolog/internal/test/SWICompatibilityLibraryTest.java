package org.cs3.pl.tuprolog.internal.test;

import junit.framework.TestCase;

import org.cs3.pl.tuprolog.internal.SWICompatibilityLibrary;

import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.event.OutputEvent;
import alice.tuprolog.event.OutputListener;

public class SWICompatibilityLibraryTest extends TestCase {
	private Prolog engine;
	
	protected void setUp() throws Exception {
		super.setUp();
		if ( engine==null){
			engine = new Prolog();
			engine.loadLibrary(new SWICompatibilityLibrary());
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testStructeqAtoms() throws	MalformedGoalException,
											NoSolutionException {
		SolveInfo info = engine.solve("a=@=b.");
		assertFalse("Failed to Query engine", info.isSuccess());
	}

	public void testStructeqVars() throws	MalformedGoalException,
											NoSolutionException {

		SolveInfo info = engine.solve("_=@=_.");
		assertTrue("Failed to Query engine", info.isSuccess());
	}
	
	public void testStructeqCompoud() throws	MalformedGoalException,
												NoSolutionException {

		SolveInfo info = engine.solve("x(B,3,a,B)=@=x(A,3,b,A).");
		assertFalse("Failed to Query engine", info.isSuccess());
	}

	public void testStructeqCompoud0() throws	MalformedGoalException,
												NoSolutionException {
		
		SolveInfo info = engine.solve("x(B,3,a,B)\\=@=x(A,3,b,A).");
		assertTrue("Failed to Query engine", info.isSuccess());
	}
	
	public void testStructeqCompoud1() throws	MalformedGoalException,
												NoSolutionException {
		
		SolveInfo info = engine.solve("x(B,C,B,B)=@=x(A,C,A,A).");
		assertTrue("Failed to Query engine", info.isSuccess());
	}

	public void testStructeqCompoud2() throws	MalformedGoalException,
												NoSolutionException {

		SolveInfo info = engine.solve("x(A,A,C,D)=@=x(A,C,D,D).");
		assertFalse("Failed to Query engine", info.isSuccess());
		info = engine.solve("x(A,A,C,D)=@=x(D,D,C,B).");
		assertTrue("Failed to Query engine", info.isSuccess());
	}
	
	public void testStructeqCompoud3() throws	MalformedGoalException,
												NoSolutionException {
		SolveInfo info = engine.solve("y(x(A,A),B)=@=y(x(B,B),A).");
		assertTrue("Failed to Query engine", info.isSuccess());
	}
	public void testStructeqList() throws	MalformedGoalException,
												NoSolutionException {

		SolveInfo info = engine.solve("[A,C,B,B]=@=[A,C,B,A].");
		assertFalse("Failed to Query engine", info.isSuccess());
	}

	public void testStructeqList2() throws	MalformedGoalException,
											NoSolutionException {

		SolveInfo info = engine.solve("[A,C,B,B]=@=[B,C,A,A].");
		assertTrue("Failed to Query engine", info.isSuccess());
	}
	
	public void testModuleOperator() throws MalformedGoalException,
											NoSolutionException {
		SolveInfo info = engine.solve("geko:assert(hasan(x)).");
		assertTrue("Failed to Query Engine ", info.isSuccess());
		info = engine.solve("hasan(x).");
		assertTrue("Failed to Query Engine ", info.isSuccess());
	}
	
	public void testThrow() throws Exception {
		
			SolveInfo inf = engine.solve("assert(example(1)).");
			SolveInfo info = engine.solve(" example(A), catch( " +
										  "throw('testing')," +
										  "'testing'," +
										  "recorda('throw_test',testing)" +
										  "), format('test: ', A).");
			
			System.err.println(info.getBindingVars());
			assertTrue("Failed to throw an exception", info.isSuccess());
			
			info = engine.solve("recorded('throw_test',X).");
			assertTrue("Failed to find a record with throw_test", info.isSuccess());
			assertEquals("testing",info.getVarValue("X").toString());
		
	}
	
	public void testThrowBindings() throws Exception{
		engine.addOutputListener(new OutputListener(){

			public void onOutput(OutputEvent e) {
				// TODO Auto-generated method stub
				System.err.println(e.getMsg());
			}
			
		});
		
		SolveInfo info = engine.solve("assert(session_id(22)).");
		info = engine.solve("catch( " +
				"throw(session_id(22))," +
				"session_id(X)," +
				"format('sessoin_id = ~w~n',X)).");
		
		assertTrue(info.isSuccess());
		
	}
	
	public void testRecorda_2() throws Exception {

		SolveInfo info = engine.solve("recorda(hasan,ops).");
		assertTrue(info.isSuccess());
	}

	public void testRecorda_3() throws Exception {
		
		SolveInfo info = engine.solve("recorda(hasan,ops, Ref).");
		assertTrue(info.isSuccess());
		System.err.println("Ref :" + info.getVarValue("Ref"));
	}

	public void testRecordz_2() throws Exception {
		
		SolveInfo info = engine.solve("recordz(hasan,ops).");
		assertTrue(info.isSuccess());		
	}

	public void testRecordz_3() throws Exception {

		SolveInfo info = engine.solve("recordz(hasan,ops, Ref).");
		assertTrue(info.isSuccess());
		System.err.println("Ref :" + info.getVarValue("Ref"));
	}

	public void testRecorded_2() throws Exception {
		
		SolveInfo info = engine.solve("recordz(hasan,ops).");
		assertTrue(info.isSuccess());
		info = engine.solve("recorded(hasan,ops).");
		assertTrue(info.isSuccess());
	}

	public void testRecorded_3() throws Exception {
		
		SolveInfo info = engine.solve("recordz(hasan,ops(d)).");
		assertTrue(info.isSuccess());
		info = engine.solve("recordz(hasan,ops(s)).");
		assertTrue(info.isSuccess());
		SolveInfo info2 = engine.solve("recorded(hasan, X, Ref).");
		assertTrue(info2.isSuccess());	
		
		System.err.println(info2.getBindingVars());
		if ( info2.hasOpenAlternatives()) {
			System.err.println("I am here");
			info2 = engine.solveNext();
			System.err.println(info2.getBindingVars());
		}
		
		//assertEquals(info.getVarValue("Ref"), info2.getVarValue("Ref"));
		//System.err.println("Ref:"+ info.getVarValue("Ref"));
	}
	
	public void testErase_1() throws Exception {
		
		SolveInfo info = engine.solve("recordz(hasan,ops, Ref).");
		assertTrue(info.isSuccess());
		
		SolveInfo info2 = engine.solve("recorded(hasan,ops, Ref), erase(Ref).");
		assertTrue(info2.isSuccess());	
		
		info2 = engine.solve("recorded(hasan,ops, Ref).");
		assertFalse(info2.isSuccess());	
	}
	
	public void testWithMutex() throws Exception{
		SolveInfo info = engine.solve("with_mutex('mutex_test',recordz(hasan,ops, Ref)).");
		assertTrue(info.isSuccess());

	}
}
