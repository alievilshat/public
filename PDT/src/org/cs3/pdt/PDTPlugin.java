package org.cs3.pdt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.cs3.pdt.internal.PDTPrologHelper;
import org.cs3.pdt.internal.editors.PLEditor;
import org.cs3.pdt.internal.views.PrologNode;
import org.cs3.pl.common.Debug;
import org.cs3.pl.common.DefaultResourceFileLocator;
import org.cs3.pl.common.ResourceFileLocator;
import org.cs3.pl.common.Util;
import org.cs3.pl.metadata.IMetaInfoProvider;
import org.cs3.pl.metadata.SourceLocation;
import org.cs3.pl.prolog.ConsultService;
import org.cs3.pl.prolog.Option;
import org.cs3.pl.prolog.PrologInterface;
import org.cs3.pl.prolog.LifeCycleHook;
import org.cs3.pl.prolog.PrologInterfaceFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PDTPlugin extends AbstractUIPlugin implements IAdaptable {

    private static final String EP_INIT_HOOK = "hooks";

    public static final String MODULEPREFIX = "pdtplugin:";

    //The shared instance.
    private static PDTPlugin plugin;

    /**
     * Returns the shared instance.
     */
    public static PDTPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = PDTPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    private HashMap consultServices = new HashMap();

    private ConsultService metadataConsultService;

    private String pdtModulePrefix = "";

    private PDTPrologHelper prologHelper;

    private PrologInterface prologInterface;

    //Resource bundle.
    private ResourceBundle resourceBundle;

    private ConsultService workspaceConsultService;

    private Object root;

    private DefaultResourceFileLocator rootLocator;

    //private HashMap locators=new HashMap();

    /**
     * The constructor.
     */
    public PDTPlugin() {
        super();
        plugin = this;
        try {
            resourceBundle = ResourceBundle
                    .getBundle("prg.cs3.pdt.PDTPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    public ResourceFileLocator getResourceLocator(String key) {        
        if(rootLocator==null){
            URL url = PDTPlugin.getDefault().getBundle().getEntry("/");
            String location = null;
            try {
                location = new File(Platform.asLocalURL(url).getFile())
                        .getAbsolutePath();
            } catch (IOException t) {
                Debug.report(t);
                throw new RuntimeException(t);
            }
            if (location.charAt(location.length() - 1) == File.separatorChar){
                location = location.substring(0, location.length() - 1);
            }
            rootLocator=new DefaultResourceFileLocator(location);
        }
        return rootLocator.subLocator(key);
    }

    public IEditorPart getActiveEditor() {
        try {
            IWorkbenchPage page = getActivePage();
            return page.getActiveEditor();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public IWorkbenchPage getActivePage() {
        return getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

    /**
     * Retrieve a registered ConsultService instance. The Plugin may offer
     * several ConsultServices for different purposes. A service that manages
     * predicate meta information for the components of the prolog ide is always
     * be available using the key PDT.CS_METADATA
     * 
     * @param key
     * @return
     */
    public ConsultService getConsultService(String key) {
        synchronized (consultServices) {
            return (ConsultService) consultServices.get(key);
        }
    }

    public Display getDisplay() {
        return getWorkbench().getDisplay();
    }

    public IMetaInfoProvider getMetaInfoProvider() {
        if (prologHelper == null) {
            prologHelper = new PDTPrologHelper(prologInterface, pdtModulePrefix);
        }
        return prologHelper;
    }

    public void showSourceLocation(SourceLocation loc) {
        IFile file = null;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IPath fpath;
        try {
            fpath = new Path(new File(loc.file).getCanonicalPath());
        } catch (IOException e1) {
            Debug.report(e1);
            return;
        }
        IFile[] files = root.findFilesForLocation(fpath);
        if (files == null || files.length == 0) {
            Debug.warning("Not in Workspace: " + fpath);
            return;
        }
        if (files.length > 1) {
            Debug.warning("Mapping into workspace is ambiguose:" + fpath);
            Debug.warning("i will use the first match found: " + files[0]);
        }
        file = files[0];
        if (!file.isAccessible()) {
            Debug.warning("The specified file \"" + file
                    + "\" is not accessible.");
            return;
        }
        IWorkbenchPage page = PDTPlugin.getDefault().getActivePage();
        IEditorPart part;
        try {
            part = IDE.openEditor(page, file);
        } catch (PartInitException e) {
            Debug.report(e);
            return;
        }
        if (part instanceof PLEditor) {
            PLEditor editor = (PLEditor) part;
            editor.gotoLine(loc.line);
        }
    }

    /**
     * @return the prolog interface instance shared among this plugin's
     *               components.
     * @throws IOException
     */
    public PrologInterface getPrologInterface() throws IOException {
        if(prologInterface==null){
            IPreferencesService service = Platform.getPreferencesService();
            String qualifier = getBundle().getSymbolicName();
            String impl= service.getString(qualifier,PDT.PREF_PIF_IMPLEMENTATION,null,  null);         
             if (impl == null) {
                throw new RuntimeException("The required property \"" + PDT.PREF_PIF_IMPLEMENTATION
                        + "\" was not specified.");
             }
            PrologInterfaceFactory factory = PrologInterfaceFactory.newInstance(impl);
            factory.setResourceLocator(getResourceLocator(PDT.LOC_PIF));
            prologInterface=factory.create();            
           
            reconfigurePrologInterface();
        }
        return prologInterface;
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see prg.cs3.pdt.PreferenceListener#preferencesChanged(prg.cs3.pdt.PreferencesEvent)
     */
    protected void preferenceChanged(PropertyChangeEvent e) {
        String key = e.getProperty();
        if (key.equals(PDT.PREF_SERVER_PORT) || key.equals(PDT.PREF_SWIPL_DIR)
                || key.equals(PDT.PREF_SERVER_CLASSPATH)
                || key.equals(PDT.PREF_USE_SESSION_POOLING)
                || key.equals(PDT.PREF_DEBUG_LEVEL)
                || key.equals(PDT.PREF_SERVER_STANDALONE)) {
            try {
                prologInterface.stop();
                reconfigurePrologInterface();
                prologInterface.start();
            } catch (IOException e1) {
                Debug.report(e1);
            }
        }

    }

    /**
     *  
     */
    public void reconfigure() {
        try {
            IPreferencesService service = Platform.getPreferencesService();
            String qualifier = getBundle().getSymbolicName();
            String debugLevel = service.getString(qualifier,
                    PDT.PREF_DEBUG_LEVEL, "ERROR", null);
            Debug.setDebugLevel(debugLevel);
            prologInterface.stop();
            reconfigurePrologInterface();
            

            prologInterface.start();
        } catch (Throwable e) {
            Debug.report(e);
        }

    }

    

    private void reconfigurePrologInterface() {
        
        IPreferencesService service = Platform.getPreferencesService();
        String qualifier = getBundle().getSymbolicName();
        String impl= service.getString(qualifier,PDT.PREF_PIF_IMPLEMENTATION,null,  null);         
         if (impl == null) {
            throw new RuntimeException("The required property \"" + PDT.PREF_PIF_IMPLEMENTATION
                    + "\" was not specified.");
         }
        List l = prologInterface.getBootstrapLIbraries();
        l.clear();
        l.add(Util.prologFileName(getResourceLocator(PDT.LOC_ENGINE).resolve("main.pl")));
        PrologInterfaceFactory factory = prologInterface.getFactory();      
        Option[] options = factory.getOptions();
        for(int i=0;i<options.length;i++){
            String id=options[i].getId();
            String val = service.get(id,options[i].getDefault(),null);
            prologInterface.setOption(id,val);
        }
        
    }

    public void registerConsultService(String key, ConsultService consultService) {
        synchronized (consultServices) {
            consultServices.put(key, consultService);
        }
    }

    /**
     * Looks up all avaible extensions for the extension point
     * org.cs3.pl.extension.factbase.updated, creates Observer objects and calls
     * their update() methods.
     * 
     * @param project
     * @param prologManager
     * 
     * @throws CoreException
     */
    protected boolean registerHooks() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("org.cs3.pdt",
                EP_INIT_HOOK);
        if (point == null) {
            Debug.error("could not find the extension point " + EP_INIT_HOOK);
            return false;
        }
        IExtension[] extensions = point.getExtensions();
        try {
            for (int i = 0; i < extensions.length; i++) {
                IConfigurationElement[] celem = extensions[i]
                        .getConfigurationElements();
                for (int j = 0; j < celem.length; j++) {

                    if (!celem[j].getName().equals("hook")) {
                        Debug.warning("hmmm... asumed a hook, but got a "
                                + celem[j].getName());
                    } else {
                        LifeCycleHook hook = (LifeCycleHook) celem[j]
                                .createExecutableExtension("class");
                        String dependsOn = celem[j]
                                .getAttributeAsIs("dependsOn");
                        if (dependsOn == null) {
                            dependsOn = "";
                        }
                        String[] dependencies = dependsOn.split(",");
                        String id = celem[j].getAttributeAsIs("id");
                        prologInterface
                                .addLifeCycleHook(hook, id, dependencies);
                    }
                }
            }
        } catch (CoreException e) {
            Debug.report(e);
            return false;
        }
        return true;
    }

    public void setStatusErrorMessage(final String string) {
        getDisplay().asyncExec(new Runnable() {
            public void run() {
                getActiveEditor().getEditorSite().getActionBars()
                        .getStatusLineManager().setErrorMessage(string);
            }
        });
    }

    public void setStatusMessage(final String string) {
        getDisplay().asyncExec(new Runnable() {
            public void run() {
                getActiveEditor().getEditorSite().getActionBars()
                        .getStatusLineManager().setMessage(string);
            }
        });

    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        try {
            super.start(context);
            /*
             * XXX not sure if this is the "eclipse way(tm)" to go, but we need to make sure that
             * the pdt preferences are correctly initilized before proceeding.
             */
            getPluginPreferences();
            PrologInterface pif = getPrologInterface();
            metadataConsultService = pif.getConsultService(PDT.CS_METADATA);
            workspaceConsultService = pif.getConsultService(PDT.CS_WORKSPACE);
            metadataConsultService.setRecording(true);
            registerConsultService(PDT.CS_METADATA, metadataConsultService);
            registerConsultService(PDT.CS_WORKSPACE, workspaceConsultService);
            registerHooks();
            prologInterface.start();
        } catch (Throwable t) {
            Debug.report(t);
        }
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        try {
            if (prologInterface != null && !prologInterface.isDown()) {
                prologInterface.stop();
            }
        } finally {
            super.stop(context);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (IWorkbenchAdapter.class.equals(adapter)) {
            if (root == null) {
                root = new IWorkbenchAdapter() {
                    private Object[] modules;

                    public Object[] getChildren(Object o) {
                        if (modules == null) {
                            try {
                                modules = PrologNode.find(getPrologInterface(),
                                        "type(module)").toArray();
                            } catch (IOException e) {
                                Debug.report(e);
                                throw new RuntimeException(e);
                            }
                        }
                        return modules;
                    }

                    public ImageDescriptor getImageDescriptor(Object object) {
                        String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
                        return PlatformUI.getWorkbench().getSharedImages()
                                .getImageDescriptor(imageKey);
                    }

                    public String getLabel(Object o) {
                        return "Nur wo PDT drauf steht, ist auch PDT drin.";
                    }

                    public Object getParent(Object o) {
                        return null;
                    }
                };
            }
            return root;
        }
        return null;
    }


}