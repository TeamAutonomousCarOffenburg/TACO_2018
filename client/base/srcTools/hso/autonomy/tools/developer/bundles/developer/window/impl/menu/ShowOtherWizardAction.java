package hso.autonomy.tools.developer.bundles.developer.window.impl.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.developer.bundles.developer.window.Wizards;

public class ShowOtherWizardAction extends AbstractAction
{
	private final IWindowManager windowManager;

	private Wizards wizard;

	public ShowOtherWizardAction(IWindowManager windowManager, Wizards wizard)
	{
		super("Other");
		putValue(SHORT_DESCRIPTION, "Other");

		this.windowManager = windowManager;
		this.wizard = wizard;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		windowManager.showWizard(wizard);
	}
}