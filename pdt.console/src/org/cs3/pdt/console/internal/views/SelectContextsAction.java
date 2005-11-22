package org.cs3.pdt.console.internal.views;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.cs3.pdt.console.PDTConsole;
import org.cs3.pdt.console.PrologConsolePlugin;
import org.cs3.pdt.console.internal.ImageRepository;
import org.cs3.pdt.runtime.PrologContextTracker;
import org.cs3.pdt.runtime.PrologContextTrackerListener;
import org.cs3.pdt.runtime.PrologRuntimePlugin;
import org.cs3.pl.common.Util;
import org.cs3.pl.prolog.PrologInterface;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

public abstract class SelectContextsAction extends Action implements IMenuCreator,
		IWorkbenchWindowPulldownDelegate2, PrologContextTrackerListener {

	private Set activeTrackers;
	
	/**
	 * Cascading menu
	 */
	private Menu fCreatedMenu;

	/**
	 * Presentation wrapper for this action
	 */
	private IAction fAction;

	/**
	 * Creates a cascading menu action to populate with shortcuts in the given
	 * launch group.
	 * 
	 * @param launchGroupIdentifier
	 *            launch group identifier
	 */
	public SelectContextsAction() {
		super();

		setText(null);
		setImageDescriptor(ImageRepository.getImageDescriptor(ImageRepository.TRACK_CONTEXT));		
		setMenuCreator(this);
	}

	
	
	/**
	 * @see IAction#run()
	 */
	public void run() {
		// do nothing, this action just creates a cascading menu.
	}

	public PrologInterface getCurrentPrologInterface(){
		
		for (Iterator it = getActiveTrackers().iterator(); it.hasNext();) {
			String trackerId = (String) it.next();
			PrologContextTracker tracker = PrologRuntimePlugin.getDefault().getContextTrackerService().getContextTracker(trackerId); 
			PrologInterface pif = tracker.getCurrentPrologInterface();
			if(pif!=null){
				return pif;
			}
		}
		return null;
	}
	
	private void createAction(Menu parent, final PrologContextTracker tracker) {
		final String trackerId = tracker.getId();
		IAction action = new Action(tracker.getLabel(),IAction.AS_CHECK_BOX){
			public void run() {
				
				if(getActiveTrackers().contains(trackerId)){
					getActiveTrackers().remove(trackerId);
					tracker.removePrologContextTrackerListener(SelectContextsAction.this);
					
				}else{
					getActiveTrackers().add(trackerId);
					tracker.addPrologContextTrackerListener(SelectContextsAction.this);
					
				}
				PrologConsolePlugin.getDefault().setPreferenceValue(PDTConsole.PREF_CONTEXT_TRACKERS,Util.splice(getActiveTrackers(),","));
				setChecked(getActiveTrackers().contains(trackerId));
			}
		};
		action.setChecked(getActiveTrackers().contains(trackerId));
		ActionContributionItem item = new ActionContributionItem(action);
		
		item.fill(parent, -1);
	}

	/**
	 * @see IMenuCreator#dispose()
	 */
	public void dispose() {
		if (getCreatedMenu() != null) {
			getCreatedMenu().dispose();
		}
	}

	/**
	 * @see IMenuCreator#getMenu(Control)
	 */
	public Menu getMenu(Control parent) {
		if (getCreatedMenu() != null) {
			getCreatedMenu().dispose();
		}
		setCreatedMenu(new Menu(parent));
		fillMenu();
		initMenu();
		return getCreatedMenu();
	}

	/**
	 * @see IMenuCreator#getMenu(Menu)
	 */
	public Menu getMenu(Menu parent) {
		if (getCreatedMenu() != null) {
			getCreatedMenu().dispose();
		}
		setCreatedMenu(new Menu(parent));
		fillMenu();
		initMenu();
		return getCreatedMenu();
	}

	private void fillMenu() {
		PrologContextTracker[] trackers = PrologRuntimePlugin.getDefault().getContextTrackerService().getContextTrackers();
		Menu menu=getCreatedMenu();
		for (int i = 0; i < trackers.length; i++) {
			
			createAction(menu, trackers[i]);
		}
	}

	/**
	 * Creates the menu for the action
	 */
	private void initMenu() {
		// Add listener to repopulate the menu each time
		// it is shown to reflect changes in selection or active perspective
		fCreatedMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu) e.widget;
				MenuItem[] items = m.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu();
			}
		});
	}

	

	private Menu getCreatedMenu() {
		return fCreatedMenu;
	}

	private void setCreatedMenu(Menu createdMenu) {
		fCreatedMenu = createdMenu;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		// if (window instanceof WorkbenchWindow) {
		// fKeyBindingService= ((WorkbenchWindow)window).getKeyBindingService();
		// }
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// do nothing - this is just a menu
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (fAction == null) {
			initialize(action);
		}
	}

	/**
	 * Set the enabled state of the underlying action based on whether there are
	 * any registered launch shortcuts for this launch mode.
	 */
	private void initialize(IAction action) {
		fAction = action;

	}



	



	Set getActiveTrackers() {
		if(activeTrackers==null){
			activeTrackers=new HashSet();			
			Util.split(PrologConsolePlugin.getDefault().getPreferenceValue(PDTConsole.PREF_CONTEXT_TRACKERS,""),",",activeTrackers);			
		}
		return activeTrackers;
	}

}
