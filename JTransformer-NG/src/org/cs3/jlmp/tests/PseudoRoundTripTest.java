package org.cs3.jlmp.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.cs3.jlmp.JLMP;
import org.cs3.jlmp.JLMPPlugin;
import org.cs3.jlmp.JLMPProject;
import org.cs3.pl.common.Debug;
import org.cs3.pl.common.ResourceFileLocator;
import org.cs3.pl.common.Util;
import org.cs3.pl.prolog.PrologInterface;
import org.cs3.pl.prolog.PrologSession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;

/**
 * Testing for bytecode invariance of fact generation / source re-generation
 * roundtripp.
 * 
 * 
 * <p>
 * This testcase will
 * <ul>
 * <li>setup the converter testproject</li>
 * <li>traverse all packeges starting with "test"</li>
 * <li>for each package
 * <ul>
 * <li>create prolog facts for all files in this package.</li>
 * <li>consult the generated facts</li>
 * <li>normalize all source files in that package</li>
 * <li>compile all files in the package</li>
 * <li>rename the resulting class files by attaching the prefix ".orig" This
 * set of files is until now adressed as "the original bytecode"</li>
 * <li>rename the normalized source files by attaching the prefix ".orig" Those
 * files will be adressed as "the original source code"</li>
 * <li>regenerate the source code of all toplevels present in the prolog system
 * </li>
 * <li>normalize the resulting source files in the package. These files will
 * from now on be called "the generated sourcecode"</li>
 * <li>Assert that for each original source file there is a generated source
 * file with corresponding name.</li>
 * <li>Assert that for each generated source file there is an original source
 * file with corresponding name.</li>
 * <li>compile all files in the package, from now on adressed as "the generated
 * bytecode"</li>
 * <li>assert that for each original bytecode file there is a generated
 * bytecode file with corresponding name.</li>
 * <li>assert that for each generated bytecode file there is an original
 * bytecode file with corresponding name.</li>
 * <li>assert that each corresponding pair of original and generated bytecode
 * files is binary identical.</li>
 * </ul>
 * </li>
 * </ul>
 *  
 */
public class PseudoRoundTripTest extends FactGenerationTest {

    private final class Comparator implements IResourceVisitor {
        public boolean visit(IResource resource) throws CoreException {
            switch (resource.getType()) {
            case IResource.FOLDER:
                return true;
            case IResource.FILE:
                IFile file = (IFile) resource;
                if (!file.getFileExtension().equals("class"))
                    return false;

                IFile orig = ResourcesPlugin.getWorkspace().getRoot().getFile(
                        file.getFullPath().addFileExtension("orig"));
                assertTrue(packageName
                        + ": original class file not accessible: "
                        + orig.getFullPath().toString(), orig.isAccessible());
                //both files should be of EXACTLY the same size:

                BufferedReader origReader = new BufferedReader(
                        new InputStreamReader(orig.getContents()));
                BufferedReader genReader = new BufferedReader(
                        new InputStreamReader(file.getContents()));
                int origR = 0;
                int genR = 0;
                int i = 0;
                for (i = 0; origR != -1 && genR != -1; i++) {
                    try {
                        origR = origReader.read();
                        genR = genReader.read();
                        assertTrue(
                                packageName
                                        + ": orig and generated file differ at position "
                                        + i + ": " + orig.getName(),
                                origR == genR);
                    } catch (IOException e) {
                        org.cs3.pl.common.Debug.report(e);
                    }
                }
                try {
                    origReader.close();
                    genReader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                org.cs3.pl.common.Debug.info("compared " + i
                        + " chars succsessfully.");
                return false;

            }
            return false;
        }
    }

    private final class Renamer implements IResourceVisitor {
        String[] extensions = null;

        String suffix = null;

        public Renamer(String extensions[], String suffix) {
            this.extensions = extensions;
            this.suffix = suffix;
        }

        public boolean visit(IResource resource) throws CoreException {
            switch (resource.getType()) {
            case IResource.FOLDER:
                return true;
            case IResource.FILE:
                IFile file = (IFile) resource;
                if (!file.isAccessible()) {
                    Debug.warning("RENAMER:not accsessible: "
                            + file.getFullPath());
                    break;
                }
                if (extensions == null || extensions.length == 0) {
                    file.move(file.getFullPath().addFileExtension(suffix),
                            true, null);
                    break;
                }
                for (int i = 0; i < extensions.length; i++) {
                    if (extensions[i].equals(file.getFileExtension())) {

                        try {
                            file.move(file.getFullPath().addFileExtension(
                                    suffix), true, null);
                        } catch (Throwable t) {
                            Debug.report(t);
                        }

                        break;
                    }
                }
                break;
            case IResource.PROJECT:
                return true;
            default:
                throw new IllegalStateException("Unexpected resource type.");
            }
            return false;
        }
    }

    private String packageName;

    private PrologSession session;

    private boolean passed;

    /**
     * @param name
     */
    public PseudoRoundTripTest(String name) {
        super(name);
        this.packageName = name;
    }

    /**
     * @param string
     * @param string2
     */
    public PseudoRoundTripTest(String name, String packageName) {
        super(name);

        this.packageName = packageName;
    }

    protected Object getKey() {

        return PseudoRoundTripTest.class;
    }

    public void setUpOnce() throws Exception {
        super.setUpOnce();
        PrologInterface pif = getTestJLMPProject().getPrologInterface();

        synchronized (pif) {

            //install test workspace
            ResourceFileLocator l = JLMPPlugin.getDefault().getResourceLocator(
                    "");
            File r = l.resolve("testdata-roundtrip.zip");
            Util.unzip(r);
            org.cs3.pl.common.Debug
                    .info("setUpOnce caled for key  " + getKey());
            setAutoBuilding(false);

            try {
                pif.getConsultService(JLMP.SRC).clearRecords();
                pif.getConsultService(JLMP.EXT).clearRecords();
                pif.stop();
                assertTrue(pif.isDown());
                pif.start();
                pif.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void testIt() throws CoreException, IOException,
            BadLocationException, InterruptedException {
        PrologInterface pif = getTestJLMPProject().getPrologInterface();
        synchronized (pif) {
            testIt_impl();
            passed = true;
        }

    }

    public synchronized void testIt_impl() throws CoreException, IOException,
            BadLocationException, InterruptedException {

        Util.startTime("untilBuild");
        IProject project = getTestProject();
        IJavaProject javaProject = getTestJavaProject();
        JLMPProject jlmpProject = getTestJLMPProject();
        PrologInterface pif = jlmpProject.getPrologInterface();

        org.cs3.pl.common.Debug.info("Running (Pseudo)roundtrip in "
                + packageName);
        //retrieve all cus in package
        ICompilationUnit[] cus = getCompilationUnitsRecursive(packageName);
        //normalize source files
        Util.startTime("norm1");
        normalize(cus);
        Util.printTime("norm1");
        Util.printTime("untilBuild");
        Util.startTime("build1");
        //		IFile javaFile = project.getFolder(packageName).getFile("Test.java");
        //		assertTrue(javaFile.isSynchronized(IResource.DEPTH_INFINITE));
        //		assertTrue(javaFile.exists());

        build(JavaCore.BUILDER_ID);
        //Thread.sleep(1000);
        //		IFile classFile =
        // project.getFolder(packageName).getFile("Test.class");
        //		assertTrue(classFile.isSynchronized(IResource.DEPTH_INFINITE));
        //		assertTrue(classFile.exists());

        build(JLMP.BUILDER_ID);

        Util.printTime("build1");
        Util.startTime("untilQueryToplevels");
        //now we should have SOME toplevelT
        assertNotNull(packageName + ": no toplevelT????", session
                .queryOnce("toplevelT(_,_,_,_)"));

        //and checkTreeLinks should say "yes"
        //assertNotNull("checkTreeLinks reports errors",
        // session.queryOnce("checkTreeLinks"));

        Util.startTime("rename");
        IResource folder = project.getFolder(packageName);
        rename(folder, new String[] { "java", "class" }, "orig");

        //		classFile =
        // project.getFolder(packageName).getFile("Test.class.orig");
        //		assertTrue(classFile.isSynchronized(IResource.DEPTH_INFINITE));
        //		assertTrue(classFile.exists());

        Util.printTime("rename");

        //next, we use gen_tree on each toplevelT node known to the system.
        //as a result we should be able to regenerate each and every source
        // file we consulted
        //in the first step
        generateSource();
        Util.printTime("writeToplevels");
        //refetch cus
        Util.startTime("norm2");
        cus = getCompilationUnitsRecursive(packageName);
        normalize(cus);
        Util.printTime("norm2");
        //build again.(the generated source)
        Util.startTime("build2");

        build(JavaCore.BUILDER_ID);
        Util.printTime("build2");
        //now, visit each file in the binFolder, that has the .class extension.
        //and compare it to the respective original class file (which should
        // have the same name + .orig)
        Util.startTime("compare");
        compare(folder);
        Util.printTime("compare");
    }

    /**
     * @param cus
     * @throws CoreException
     */
    private void normalize(final ICompilationUnit[] cus) throws CoreException {

        for (int i = 0; i < cus.length; i++) {
            ICompilationUnit cu = cus[i];

            try {
                normalizeCompilationUnit(cu);
            } catch (Exception e) {
                throw new RuntimeException(packageName
                        + ": could not normalize cu " + cu.getElementName(), e);
            }
        }

    }

    /**
     * @param folder
     * @throws CoreException
     */
    private void compare(final IResource folder) throws CoreException {
        final IResourceVisitor comparator = new Comparator();
        IWorkspaceRunnable r = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    folder.accept(comparator);
                } catch (Throwable e) {
                    Debug.report(e);
                    throw new RuntimeException(e);
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(r, getTestProject(),
                IWorkspace.AVOID_UPDATE, null);
    }

    /**
     * @param folder
     * @throws CoreException
     */
    private void rename(final IResource root, String[] exts, String suffix)
            throws CoreException {
        final IResourceVisitor renamer = new Renamer(exts, suffix);
        IWorkspaceRunnable r = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    root.accept(renamer);
                    root.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (Throwable e) {
                    Debug.report(e);
                    throw new RuntimeException(e);
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(r, getTestProject(),
                IWorkspace.AVOID_UPDATE, null);
    }

    protected synchronized void setUp() throws Exception {
        super.setUp();
        PrologInterface pif = getTestJLMPProject().getPrologInterface();
        assertTrue(pif.isUp());
        session = pif.getSession();
        if (session == null) {
            fail("failed to obtain session");
        }
        ResourceFileLocator l = JLMPPlugin.getDefault().getResourceLocator(
                "testdata-roundtrip");
		setTestDataLocator(l);
		
        install(packageName);
//        if(l.resolve(packageName+"_a").exists()){
//        	install(packageName+"_a");
//        }
        passed = false;

    }

   
    protected synchronized void tearDown() throws Exception {
        super.tearDown();
        PrologInterface pif = getTestJLMPProject().getPrologInterface();
        synchronized (pif) {

            session.dispose();
            if (passed) {
                uninstall(packageName);
            } else {
                //if anything breaks,
                //we must make sure not interfere with consecutive tests.
                //1) move any left java or class files out of the way of the
                // next build
                IFolder folder = getTestProject().getFolder(packageName);
                rename(folder, new String[] { "java", "class" }, "bak");
                //2) restart the pif
                pif.getConsultService(JLMP.SRC).clearRecords();
                pif.getConsultService(JLMP.EXT).clearRecords();
                pif.stop();
                assertTrue(pif.isDown());
                pif.start();
            }
        }

    }

    public void tearDownOnce() {
        super.tearDownOnce();
        PrologInterface pif = getTestJLMPProject().getPrologInterface();
        synchronized (pif) {
            session.dispose();
            try {
                pif.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        BitSet blacklist = new BitSet();

        /*
         * these need extra libs.
         * i blacklist them for now
         */
        blacklist.set(332);
        blacklist.set(335);
        blacklist.set(336);
        
        
        /*
         * these two are missing for some reason
         */
        blacklist.set(157);
        blacklist.set(158);
        blacklist.set(170);
        blacklist.set(237);
        blacklist.set(305);
        blacklist.set(306);
        blacklist.set(316);
        blacklist.set(340);
        blacklist.set(452);
        blacklist.set(462);
        blacklist.set(472);
        blacklist.set(469);

        /*
         * ld: the following few do not compile. ergo, not our prob. interesting
         * though, the builder eats most of them.
         */
        blacklist.set(44);
        blacklist.set(78);
        blacklist.set(79);
        blacklist.set(80);
        blacklist.set(81);
        blacklist.set(86);
        blacklist.set(87);
        blacklist.set(114);
        blacklist.set(115);
        blacklist.set(118);
        blacklist.set(150);
        blacklist.set(152);
        blacklist.set(153);
        blacklist.set(182);
        blacklist.set(183);
        blacklist.set(184);
        blacklist.set(185);
        blacklist.set(186);
        blacklist.set(187);
        blacklist.set(188);
        blacklist.set(190);
        blacklist.set(191);
        blacklist.set(192);
        blacklist.set(193);
        blacklist.set(194);
        blacklist.set(196);
        blacklist.set(197);
        blacklist.set(200);
        blacklist.set(233);
        blacklist.set(234);
        blacklist.set(241);
        blacklist.set(258);
        blacklist.set(259);
        blacklist.set(261);
        blacklist.set(291);
        blacklist.set(294);
        blacklist.set(295);
        blacklist.set(296);
        blacklist.set(312);
        blacklist.set(327);
        blacklist.set(330);
        blacklist.set(339);
        blacklist.set(312);
        blacklist.set(344);
        blacklist.set(354);
        blacklist.set(356);
        blacklist.set(362);
        blacklist.set(312);
        blacklist.set(379);
        blacklist.set(398);
        blacklist.set(401);
        blacklist.set(409);
        blacklist.set(413);
        blacklist.set(417);
        blacklist.set(419);
        blacklist.set(426);
        blacklist.set(427);
        blacklist.set(431);
        blacklist.set(436);
        blacklist.set(437);
        blacklist.set(438);
        blacklist.set(439);
        blacklist.set(443);
        blacklist.set(444);
        blacklist.set(447);        
        blacklist.set(461);
        blacklist.set(466);
        blacklist.set(467);
        blacklist.set(471);        
        blacklist.set(473);
        blacklist.set(477);
        blacklist.set(487);
        blacklist.set(488);
        blacklist.set(489);
        blacklist.set(491);
        blacklist.set(492);
        blacklist.set(498);
        blacklist.set(501);
        blacklist.set(504);
        blacklist.set(505);
        blacklist.set(510);        
        blacklist.set(511);
        blacklist.set(513);
        blacklist.set(514);
        blacklist.set(516);
        blacklist.set(517);
        blacklist.set(518);
        blacklist.set(519);
        blacklist.set(520);
        blacklist.set(531);
        blacklist.set(533);
        blacklist.set(534);
        blacklist.set(535);
        blacklist.set(536);
        
        for (int i = 1; i <= 539; i++) {//1-539
            if (!blacklist.get(i)) {
                s.addTest(new PseudoRoundTripTest("testIt",
                        generatePackageName(i)));
            }
        }
        s.setName("PseudoRoundtripTest");
        return s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#getName()
     */
    public String getName() {
        return packageName;
    }

    /**
     * @param i
     * @return
     */
    private static String generatePackageName(int n) {
        int desiredLength = 4;
        String number = String.valueOf(n);
        int padLength = desiredLength - number.length();
        StringBuffer sb = new StringBuffer("test");
        for (int i = 0; i < padLength; i++)
            sb.append('0');
        sb.append(number);
        return sb.toString();
    }

}