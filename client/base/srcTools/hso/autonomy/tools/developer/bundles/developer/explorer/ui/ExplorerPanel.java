/*******************************************************************************
 * Copyright 2008, 2012 Hochschule Offenburg
 * Klaus Dorer, Mathias Ehret, Stefan Glaser, Thomas Huber, Fabian Korak,
 * Simon Raffeiner, Srinivasa Ragavan, Thomas Rinklin,
 * Joachim Schilling, Ingo Schindler, Rajit Shahi, Bjoern Weiler
 *
 * This file is part of magmaOffenburg.
 *
 * magmaOffenburg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * magmaOffenburg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with magmaOffenburg. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package hso.autonomy.tools.developer.bundles.developer.explorer.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.explorer.model.IExplorer;
import hso.autonomy.tools.developer.bundles.developer.explorer.model.IExplorerListener;
import hso.autonomy.tools.developer.bundles.developer.explorer.model.ITreeNode;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewDescriptor;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.developer.bundles.developer.window.Wizards;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;
import hso.autonomy.tools.util.imageBuffer.ImageFile;
import hso.autonomy.tools.util.swing.wizard.IWizardDescriptor;
import hso.autonomy.tools.util.swing.wizard.Wizard;

public class ExplorerPanel extends JPanel implements IExplorerListener
{
	private final BundleContext context;

	private final IExplorer model;

	private final IWindowManager windowManager;

	private ExplorerTreeModel treeModel;

	private ExplorerTree tree;

	private JMenu newMenu;

	private JMenu openMenu;

	private JMenu openWithMenu;

	private JMenu openInMenu;

	private JMenu runMenu;

	private JMenu importMenu;

	private JMenu exportMenu;

	private JMenuItem openNewWizardMenuItem;

	private JMenuItem openImportWizardMenuItem;

	private JMenuItem openExportWizardMenuItem;

	private JMenuItem openRunWizardMenuItem;

	private IPerspectiveManager perspectiveManager;

	public ExplorerPanel(BundleContext context)
	{
		this.context = context;
		model = IDeveloperService.EXPLORER.get(context);
		windowManager = IDeveloperService.WINDOW_MANAGER.get(context);

		initializeComponents();
		initializeValues();

		this.model.addListener(this);
	}

	private void initializeComponents()
	{
		setLayout(new BorderLayout());

		// Create menus
		// New menu
		newMenu = new JMenu("New");
		newMenu.setIcon(ImageFile.ADD.getIcon());
		newMenu.setMnemonic(KeyEvent.VK_N);

		openNewWizardMenuItem = new JMenuItem(new ShowWizardAction("Other...", null, Wizards.NEW_WIZARD));
		openNewWizardMenuItem.setMnemonic(KeyEvent.VK_O);
		openNewWizardMenuItem.setToolTipText("Open New wizard overview.");

		// Open menu
		openMenu = new JMenu("Open...");
		openMenu.setIcon(ImageFile.OPEN_FILE.getIcon());
		openMenu.setMnemonic(KeyEvent.VK_O);
		List<ExtensionHandle<AbstractAction>> openActions =
				context.getExtensions(IDeveloperBundle.EPT_OPEN_ACTION, AbstractAction.class);
		for (ExtensionHandle<AbstractAction> handle : openActions) {
			openMenu.add(new JMenuItem(handle.get()));
		}

		// Open with and Open in menus
		openWithMenu = new JMenu("Open With...");
		openInMenu = new JMenu("Open In...");

		// Run menu
		runMenu = new JMenu("Run");
		runMenu.setIcon(ImageFile.EXECUTE.getIcon());
		runMenu.setMnemonic(KeyEvent.VK_R);

		openRunWizardMenuItem = new JMenuItem(new ShowWizardAction("Other...", null, Wizards.RUN_WIZARD));
		openRunWizardMenuItem.setMnemonic(KeyEvent.VK_O);
		openRunWizardMenuItem.setToolTipText("Open Run wizard overview.");

		// Import menu
		importMenu = new JMenu("Import");
		importMenu.setIcon(ImageFile.DOWN.getIcon());
		importMenu.setMnemonic(KeyEvent.VK_I);

		openImportWizardMenuItem = new JMenuItem(new ShowWizardAction("Other...", null, Wizards.IMPORT_WIZARD));
		openImportWizardMenuItem.setMnemonic(KeyEvent.VK_O);
		openImportWizardMenuItem.setToolTipText("Open Import wizard overview.");

		// Export menu
		exportMenu = new JMenu("Export");
		exportMenu.setIcon(ImageFile.DOWN.getIcon());
		exportMenu.setMnemonic(KeyEvent.VK_E);

		openExportWizardMenuItem = new JMenuItem(new ShowWizardAction("Other...", null, Wizards.EXPORT_WIZARD));
		openExportWizardMenuItem.setMnemonic(KeyEvent.VK_O);
		openExportWizardMenuItem.setToolTipText("Open Export wizard overview.");
		JPopupMenu.setDefaultLightWeightPopupEnabled(true);

		// Create Explorer tree
		treeModel = new ExplorerTreeModel();

		tree = new ExplorerTree(treeModel);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new ExplorerTreeNodeRenderer());
		tree.setScrollsOnExpand(true);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger()) {
					createAndShowPopupMenu(e);
				}

				if (e.getClickCount() == 2) {
					doubleClickObject(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger()) {
					createAndShowPopupMenu(e);
				}
			}
		});
		ToolTipManager.sharedInstance().registerComponent(tree);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
	}

	private void createAndShowPopupMenu(MouseEvent e)
	{
		// Check if we should select a new TreeNode, before showing the popup
		ITreeNode selectedTreeNode = getSelectedTreeNode(e);
		Object selectedObject = null;

		// Fetch selected tree node object
		if (selectedTreeNode != null) {
			selectedObject = selectedTreeNode.getUserObject();
		}

		// Create default menus
		JPopupMenu popupMenu = new JPopupMenu();

		popupMenu.add(newMenu);
		popupMenu.add(openMenu);
		popupMenu.addSeparator();
		popupMenu.add(openWithMenu);
		popupMenu.add(openInMenu);
		popupMenu.addSeparator();
		popupMenu.add(runMenu);
		popupMenu.addSeparator();
		popupMenu.add(importMenu);
		// there are currently no export wizards
		// popupMenu.add(exportMenu);

		// Refresh menus
		refreshMenus(selectedObject);

		// Create selection-specific menus
		if (selectedTreeNode != null && selectedTreeNode.getUserActions().size() > 0) {
			popupMenu.addSeparator();
			selectedTreeNode.getUserActions().forEach(popupMenu::add);
		}
		JPopupMenu.setDefaultLightWeightPopupEnabled(true);

		// Show popup menu
		popupMenu.show(ExplorerPanel.this, e.getX(), e.getY());
	}

	private ITreeNode getSelectedTreeNode(MouseEvent e)
	{
		ITreeNode selectedTreeNode = null;
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		if (path != null) {
			tree.setSelectionPath(path);
			selectedTreeNode = (ITreeNode) path.getLastPathComponent();
		} else if (tree.getSelectionPath() != null) {
			selectedTreeNode = (ITreeNode) tree.getSelectionPath().getLastPathComponent();
		}
		return selectedTreeNode;
	}

	private void doubleClickObject(MouseEvent e)
	{
		ITreeNode selectedTreeNode = getSelectedTreeNode(e);
		Object selectedObject = null;

		// Fetch selected tree node object
		if (selectedTreeNode != null) {
			selectedObject = selectedTreeNode.getUserObject();
		}

		IPerspectiveManager perspectiveManager = getPerspectiveManager();
		if (selectedObject != null && perspectiveManager != null) {
			perspectiveManager.openView(selectedObject);
		}
	}

	private void refreshMenus(Object selectedObject)
	{
		// Refresh new menu
		refreshWizardMenu(newMenu, Wizards.NEW_WIZARD, openNewWizardMenuItem);

		// Refresh Open With menu
		refreshOpenWithMenu(selectedObject);

		// Refresh Open In menu
		refreshOpenInMenu(selectedObject);

		// Refresh run menu
		refreshWizardMenu(runMenu, Wizards.RUN_WIZARD, openRunWizardMenuItem);

		// Refresh import menu
		refreshWizardMenu(importMenu, Wizards.IMPORT_WIZARD, openImportWizardMenuItem);

		// Refresh export menu
		refreshWizardMenu(exportMenu, Wizards.EXPORT_WIZARD, openExportWizardMenuItem);
	}

	private void refreshWizardMenu(JMenu menu, Wizards wizard, JMenuItem otherItem)
	{
		// Remove all elements of menu
		menu.removeAll();

		// Fetch and add preferred wizards for menu
		List<IWizardDescriptor> preferredNewWizards = windowManager.getPreferredSubWizards(wizard);

		for (IWizardDescriptor preferredNewWizard : preferredNewWizards) {
			menu.add(new JMenuItem(new OpenWizardAction(preferredNewWizard)));
		}

		// Add generic wizard to menu
		if (otherItem != null) {
			menu.addSeparator();
			menu.add(otherItem);
		}
	}

	private void refreshOpenWithMenu(Object selectedObject)
	{
		boolean success = false;

		// Clear menu
		openWithMenu.removeAll();

		if (selectedObject != null) {
			IPerspectiveManager perspectiveManager = getPerspectiveManager();

			for (IViewDescriptor view : perspectiveManager.getAvailableViews(selectedObject)) {
				openWithMenu.add(new OpenWithViewAction(view, selectedObject));
				success = true;
			}
		}

		openWithMenu.setEnabled(success);
	}

	private void refreshOpenInMenu(Object selectedObject)
	{
		boolean success = false;

		// Clear menu
		openInMenu.removeAll();

		if (selectedObject != null) {
			IPerspectiveManager perspectiveManager = getPerspectiveManager();

			for (IView view : perspectiveManager.getRegisteredViews()) {
				if (view.acceptsObject(selectedObject)) {
					openInMenu.add(new OpenInViewAction(view, selectedObject));
					success = true;
				}
			}
		}

		openInMenu.setEnabled(success);
	}

	private void initializeValues()
	{
		for (ITreeNode node : model.getActiveEntities()) {
			treeModel.getRoot().addChild(node);
		}
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	private IPerspectiveManager getPerspectiveManager()
	{
		if (perspectiveManager == null) {
			perspectiveManager = IDeveloperService.PERSPECTIVE_MANAGER.get(context);
		}
		return perspectiveManager;
	}

	@Override
	public void entityActivated(ITreeNode node)
	{
		treeModel.getRoot().addChild(node);
	}

	@Override
	public void entityDeactivated(ITreeNode node)
	{
		treeModel.getRoot().remove(node);
	}

	private class ShowWizardAction extends AbstractAction
	{
		private Wizards wizard;

		public ShowWizardAction(String title, Icon icon, Wizards wizard)
		{
			super(title, icon);
			putValue(SHORT_DESCRIPTION, title);

			this.wizard = wizard;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			windowManager.showWizard(wizard);
		}
	}

	private class OpenWithViewAction extends AbstractAction
	{
		private final IViewDescriptor viewDescriptor;

		private final Object userObject;

		public OpenWithViewAction(IViewDescriptor viewDescriptor, Object userObject)
		{
			super(viewDescriptor.getTitle(), viewDescriptor.getIcon());
			this.viewDescriptor = viewDescriptor;
			this.userObject = userObject;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			getPerspectiveManager().openView(viewDescriptor, userObject);
		}
	}

	private class OpenInViewAction extends AbstractAction
	{
		private final IView view;

		private final Object userObject;

		public OpenInViewAction(IView view, Object userObject)
		{
			super(view.getName(), view.getDescriptor().getIcon());
			this.view = view;
			this.userObject = userObject;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			view.selectObject(userObject);
			getPerspectiveManager().setActiveView(view);
		}
	}

	private class OpenWizardAction extends AbstractAction
	{
		private final IWizardDescriptor wizardDescriptor;

		public OpenWizardAction(IWizardDescriptor wizardDescriptor)
		{
			super(wizardDescriptor.getTitle(), wizardDescriptor.getIcon());
			this.wizardDescriptor = wizardDescriptor;
			setToolTipText(wizardDescriptor.getToolTip());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Wizard wiz = new Wizard(null, wizardDescriptor);
			wiz.showModalDialog();
		}
	}
}
