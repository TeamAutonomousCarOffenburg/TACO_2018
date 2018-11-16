package hso.autonomy.tools.developer.bundles.developer.window.impl.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.util.bundleFramework.BundleContext;

public class ExitAction extends AbstractAction
{
	private final BundleContext context;

	private final IPerspectiveManager perspectiveManager;

	public ExitAction(BundleContext context)
	{
		super("Exit");
		putValue(SHORT_DESCRIPTION, "Close magmaDeveloper");

		this.context = context;
		perspectiveManager = IDeveloperService.PERSPECTIVE_MANAGER.get(context);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (perspectiveManager.onWindowClosing()) {
			context.requestSystemExit();
		}
	}
}
