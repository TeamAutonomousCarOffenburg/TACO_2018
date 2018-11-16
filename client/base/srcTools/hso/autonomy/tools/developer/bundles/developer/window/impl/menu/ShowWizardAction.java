package hso.autonomy.tools.developer.bundles.developer.window.impl.menu;

import hso.autonomy.tools.util.swing.wizard.IWizardDescriptor;
import hso.autonomy.tools.util.swing.wizard.Wizard;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

public class ShowWizardAction extends AbstractAction
{
	private final IWizardDescriptor wizardDescriptor;

	private final JFrame owner;

	public ShowWizardAction(JFrame owner, IWizardDescriptor wizardDescriptor)
	{
		super(wizardDescriptor.getTitle(), wizardDescriptor.getIcon());
		this.owner = owner;
		putValue(SHORT_DESCRIPTION, wizardDescriptor.getToolTip());

		this.wizardDescriptor = wizardDescriptor;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Wizard wiz = new Wizard(owner, wizardDescriptor);
		wiz.showModalDialog();
	}
}