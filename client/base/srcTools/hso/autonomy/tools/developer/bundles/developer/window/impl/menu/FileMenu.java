package hso.autonomy.tools.developer.bundles.developer.window.impl.menu;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.util.action.SaveAsActionWrapper;
import hso.autonomy.tools.developer.util.action.SaveActionWrapper;
import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.developer.bundles.developer.window.Wizards;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;
import hso.autonomy.tools.util.imageBuffer.ImageFile;
import hso.autonomy.tools.util.swing.wizard.IWizardDescriptor;

/**
 * @author Stefan Glaser
 */
public class FileMenu extends JMenu
{
	private final IWindowManager windowManager;

	private final JMenu newMenu;

	private final JMenu importMenu;

	private final JMenu exportMenu;

	private final JMenuItem openNewWizardMenuItem;

	private final JMenuItem openImportWizardMenuItem;

	private final JMenuItem openExportWizardMenuItem;

	public FileMenu(BundleContext context)
	{
		super("File");
		setMnemonic(KeyEvent.VK_F);

		IPerspectiveManager perspectiveManager = IDeveloperService.PERSPECTIVE_MANAGER.get(context);
		windowManager = IDeveloperService.WINDOW_MANAGER.get(context);

		// Create New menu
		newMenu = new JMenu("New");
		newMenu.setIcon(ImageFile.ADD.getIcon());
		newMenu.setMnemonic(KeyEvent.VK_N);

		openNewWizardMenuItem = new JMenuItem(new ShowOtherWizardAction(windowManager, Wizards.NEW_WIZARD));
		openNewWizardMenuItem.setToolTipText("Open New wizard overview.");
		openNewWizardMenuItem.setMnemonic(KeyEvent.VK_O);
		openNewWizardMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		newMenu.add(openNewWizardMenuItem);

		// Create Open-Item
		JMenu openMenu = new JMenu("Open...");
		openMenu.setIcon(ImageFile.OPEN_FILE.getIcon());
		openMenu.setMnemonic(KeyEvent.VK_O);
		List<ExtensionHandle<AbstractAction>> openActions =
				context.getExtensions(IDeveloperBundle.EPT_OPEN_ACTION, AbstractAction.class);
		for (ExtensionHandle<AbstractAction> handle : openActions) {
			openMenu.add(new JMenuItem(handle.get()));
		}

		// Create Save-Item
		JMenuItem saveItem = new JMenuItem(new SaveActionWrapper(perspectiveManager));

		// Create SaveAs-Item
		JMenuItem saveAsItem = new JMenuItem(new SaveAsActionWrapper(perspectiveManager));

		// Create Import menu
		importMenu = new JMenu("Import");
		importMenu.setIcon(ImageFile.DOWN.getIcon());
		importMenu.setMnemonic(KeyEvent.VK_I);

		openImportWizardMenuItem = new JMenuItem(new ShowOtherWizardAction(windowManager, Wizards.IMPORT_WIZARD));
		openImportWizardMenuItem.setToolTipText("Open Import wizard overview.");
		openImportWizardMenuItem.setMnemonic(KeyEvent.VK_O);
		openImportWizardMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		importMenu.add(openImportWizardMenuItem);

		// Create Export menu
		exportMenu = new JMenu("Export");
		exportMenu.setIcon(ImageFile.UP.getIcon());
		exportMenu.setMnemonic(KeyEvent.VK_E);

		openExportWizardMenuItem = new JMenuItem(new ShowOtherWizardAction(windowManager, Wizards.EXPORT_WIZARD));
		openExportWizardMenuItem.setToolTipText("Open Export wizard overview.");
		openExportWizardMenuItem.setMnemonic(KeyEvent.VK_O);
		openExportWizardMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		exportMenu.add(openExportWizardMenuItem);

		// Create Exit-Item
		JMenuItem exitItem = new JMenuItem(new ExitAction(context));

		// Add menus
		add(newMenu);
		add(openMenu);
		addSeparator();
		add(saveItem);
		add(saveAsItem);
		addSeparator();
		add(importMenu);
		// there are currently no export wizards
		// add(exportMenu);
		addSeparator();
		add(exitItem);
	}

	@Override
	public void setPopupMenuVisible(boolean b)
	{
		if (b) {
			refreshMenus();
		}

		super.setPopupMenuVisible(b);
	}

	public void refreshMenus()
	{
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		// Refresh new menu
		refreshWizardMenu(newMenu, Wizards.NEW_WIZARD, openNewWizardMenuItem);

		// Refresh import menu
		refreshWizardMenu(importMenu, Wizards.IMPORT_WIZARD, openImportWizardMenuItem);

		// Refresh export menu
		refreshWizardMenu(exportMenu, Wizards.EXPORT_WIZARD, openExportWizardMenuItem);

		JPopupMenu.setDefaultLightWeightPopupEnabled(true);
	}

	private void refreshWizardMenu(JMenu menu, Wizards wizard, JMenuItem otherItem)
	{
		// Remove all elements of menu
		menu.removeAll();

		// Fetch and add preferred wizards for menu
		List<IWizardDescriptor> preferredNewWizards = windowManager.getPreferredSubWizards(wizard);

		for (IWizardDescriptor preferredNewWizard : preferredNewWizards) {
			JMenuItem item = new JMenuItem(new ShowWizardAction(windowManager.getMainWindow(), preferredNewWizard));
			KeyStroke accelerator = preferredNewWizard.getAccelerator();
			if (accelerator != null) {
				item.setAccelerator(accelerator);
			}
			menu.add(item);
		}

		// Add generic wizard to menu
		if (otherItem != null) {
			if (preferredNewWizards.size() > 0) {
				menu.addSeparator();
			}
			menu.add(otherItem);
		}
	}
}