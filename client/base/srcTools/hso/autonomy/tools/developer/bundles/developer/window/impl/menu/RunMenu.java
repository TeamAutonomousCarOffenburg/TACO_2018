package hso.autonomy.tools.developer.bundles.developer.window.impl.menu;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.developer.bundles.developer.window.Wizards;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.swing.wizard.IWizardDescriptor;

/**
 * @author Stefan Glaser
 */
public class RunMenu extends JMenu
{
	private final IWindowManager windowManager;

	private final JMenuItem openRunWizardMenuItem;

	public RunMenu(BundleContext context)
	{
		super("Run");

		windowManager = IDeveloperService.WINDOW_MANAGER.get(context);

		// Create menu
		setMnemonic(KeyEvent.VK_R);

		openRunWizardMenuItem = new JMenuItem(new ShowOtherWizardAction(windowManager, Wizards.RUN_WIZARD));
		openRunWizardMenuItem.setToolTipText("Open Run wizard overview.");
		openRunWizardMenuItem.setMnemonic(KeyEvent.VK_O);
		openRunWizardMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		add(openRunWizardMenuItem);
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

		removeAll();

		// Fetch and add preferred wizards for menu
		List<IWizardDescriptor> preferredNewWizards = windowManager.getPreferredSubWizards(Wizards.RUN_WIZARD);

		if (preferredNewWizards.size() > 0) {
			for (IWizardDescriptor preferredNewWizard : preferredNewWizards) {
				JMenuItem item = new JMenuItem(new ShowWizardAction(windowManager.getMainWindow(), preferredNewWizard));
				KeyStroke accelerator = preferredNewWizard.getAccelerator();
				if (accelerator != null) {
					item.setAccelerator(accelerator);
				}
				add(item);
			}

			// Add generic wizard to menu
			addSeparator();
		}

		add(openRunWizardMenuItem);

		JPopupMenu.setDefaultLightWeightPopupEnabled(true);
	}
}