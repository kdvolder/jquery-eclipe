package ca.ubc.jquery.browser.menu;

import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.actions.GenerateActionGroup;
import org.eclipse.jdt.ui.actions.JavaSearchActionGroup;
import org.eclipse.jdt.ui.actions.NavigateActionGroup;
import org.eclipse.jdt.ui.actions.RefactorActionGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.NewWizardMenu;

import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.views.JQueryTreeView;

public class EclipseMenuProvider extends JQueryMenuProvider {

	public static final String GROUP_ECLIPSE_MENU = "ca.ubc.jquery.JQueryTreeView.menu.eclipse";

	private JQueryTreeView viewPart;

	// Eclipse Menu stuff
	private NewWizardMenu newWizardMenu;

	private ImportResourcesAction importAction;

	private ExportResourcesAction exportAction;

	private RefactorActionGroup refactorGroup;

	private GenerateActionGroup generateGroup;

	private NavigateActionGroup navigateGroup;

	private JavaSearchActionGroup javaSearchGroup;

	public EclipseMenuProvider(JQueryTreeView view) {
		super((JQueryTreeViewer) view.getViewer());
		viewPart = view;

		newWizardMenu = new NewWizardMenu(view.getSite().getWorkbenchWindow());
		importAction = new ImportResourcesAction(view.getSite().getWorkbenchWindow());
		exportAction = new ExportResourcesAction(view.getSite().getWorkbenchWindow());

		refactorGroup = new RefactorActionGroup(view);
		generateGroup = new GenerateActionGroup(view);
		navigateGroup = new NavigateActionGroup(view);
		javaSearchGroup = new JavaSearchActionGroup(view);
	}

	@Override
	public void dispose() {
		newWizardMenu.dispose();
		importAction.dispose();
		exportAction.dispose();

		refactorGroup.dispose();
		generateGroup.dispose();
		navigateGroup.dispose();
		javaSearchGroup.dispose();
	}

	@Override
	public void addAvailableMenu(IMenuManager menu, IStructuredSelection selection) {
		MenuManager m = new MenuManager("Eclipse...");
		menu.appendToGroup(GROUP_ECLIPSE_MENU, m);

		// menu setup
		ActionContext c = new ActionContext(selection);
		refactorGroup.setContext(c);
		generateGroup.setContext(c);
		navigateGroup.setContext(c);
		javaSearchGroup.setContext(c);

		// menu stuff
		IMenuManager n = new MenuManager("New");
		n.add(newWizardMenu);
		m.add(n);

		//		m.add(ActionFactory.CUT.create(view.getSite().getWorkbenchWindow()));
		//		m.add(ActionFactory.COPY.create(view.getSite().getWorkbenchWindow()));
		//		m.add(ActionFactory.PASTE.create(view.getSite().getWorkbenchWindow()));
		//		m.add(ActionFactory.DELETE.create(view.getSite().getWorkbenchWindow()));
		//		m.add(new Separator());

		GroupMarker g = new GroupMarker(IContextMenuConstants.GROUP_OPEN);
		m.add(new Separator());
		m.add(g);
		g = new GroupMarker(IContextMenuConstants.GROUP_SHOW);
		m.add(g);

		g = new GroupMarker(IContextMenuConstants.GROUP_REORGANIZE);
		m.add(new Separator());
		m.add(g);

		g = new GroupMarker(IContextMenuConstants.GROUP_GENERATE);
		m.add(new Separator());
		m.add(g);

		m.add(new Separator());
		m.add(importAction);
		m.add(exportAction);

		g = new GroupMarker(IContextMenuConstants.GROUP_SEARCH);
		m.add(new Separator());
		m.add(g);

		g = new GroupMarker(IContextMenuConstants.GROUP_ADDITIONS);
		m.add(new Separator());
		m.add(g);

		g = new GroupMarker(IContextMenuConstants.GROUP_PROPERTIES);
		m.add(new Separator());
		m.add(g);

		navigateGroup.fillContextMenu(m);
		generateGroup.fillContextMenu(m);
		refactorGroup.fillContextMenu(m);
		// javaSearchGroup.fillContextMenu(m);

		// Register menu for extension.
		viewPart.getSite().registerContextMenu(m, viewPart.getSelectionProvider());
	}

	@Override
	public void addMenuGroup(IMenuManager menu) {
		menu.add(new Separator(GROUP_ECLIPSE_MENU));
	}
}
