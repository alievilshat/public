package org.cs3.pdt.internal.views;

import org.cs3.pdt.PDT;
import org.cs3.pdt.PDTPlugin;
import org.cs3.pdt.internal.hooks.ConsoleServerHook;
import org.cs3.pl.common.Debug;
import org.cs3.pl.common.Util;
import org.cs3.pl.console.ConsoleView;
import org.cs3.pl.console.DefaultConsoleController;
import org.cs3.pl.prolog.LifeCycleHook;
import org.cs3.pl.prolog.PrologInterface;
import org.cs3.pl.prolog.PrologSession;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class PrologConsoleView extends ViewPart implements LifeCycleHook {
    public static final String HOOK_ID = "org.cs3.pdt.internal.views.PrologConsoleView";

    private ConsoleView view;

    private PrologSocketConsoleModel model;

    private DefaultConsoleController controller;

    private PrologCompletionProvider completionProvider;

    public PrologConsoleView() {
    }

    public void createPartControl(Composite parent) {
        try {
            createPartControl_impl(parent);
        } catch (Throwable t) {
            Debug.report(t);
            throw new RuntimeException(t.getLocalizedMessage(),t);
        }
    }

    /**
     * @param parent
     */
    private void createPartControl_impl(Composite parent) {
        PDTPlugin plugin = PDTPlugin.getDefault();
        PrologInterface pi = null;
        pi = plugin.getPrologInterface();
        
        view = new ConsoleView();
        pi.addLifeCycleHook(this, HOOK_ID, new String[] {
                ConsoleServerHook.HOOK_ID});

        controller = new DefaultConsoleController();
        completionProvider = new PrologCompletionProvider();
        completionProvider.setPrologInterface(pi);
        controller.setCompletionProvider(completionProvider);
        view.setController(controller);
        int port = getPort();
        model = new PrologSocketConsoleModel(false);
        model.setPort(port);
        view.setModel(model);
        view.createPartControl(parent);
        if (Util.probePort(port)) {
            model.connect();
        }
        //else: wait til the hook callback is called.

    }

    private  int getPort() {
        String value = PDTPlugin.getDefault().getPreferenceValue(PDT.PREF_CONSOLE_PORT, null);
        if (value==null) {
            throw new NullPointerException("Required property \""
                    + PDT.PREF_CONSOLE_PORT + "\" was not specified.");
        }
        int port = Integer.parseInt(value);        
         return port;
     }


    public void setFocus() {
        if (view == null) {
            Debug
                    .warning("PrologConsoleView.setFocus(): View not instantiated yet.");
            return;
        }
        view.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cs3.pl.prolog.LifeCycleHook#onInit(org.cs3.pl.prolog.PrologSession)
     */
    public void onInit(PrologInterface pif,PrologSession initSession) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cs3.pl.prolog.LifeCycleHook#afterInit()
     */
    public void afterInit(PrologInterface pif) {
        view.setController(controller);
        model.connect();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cs3.pl.prolog.LifeCycleHook#beforeShutdown(org.cs3.pl.prolog.PrologSession)
     */
    public void beforeShutdown(PrologInterface pif,PrologSession session) {
        view.setController(null);
        model.disconnect();
    }
}