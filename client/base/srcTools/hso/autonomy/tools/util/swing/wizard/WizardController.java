package hso.autonomy.tools.util.swing.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Stefan Glaser
 */
public class WizardController implements ActionListener
{
	private Wizard wizard;

	public WizardController(Wizard wizard)
	{
		this.wizard = wizard;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand() == Wizard.BACK_BUTTON_ACTION_COMMAND) {
			wizard.setCurrentPanel(wizard.getModel().getCurrentDescriptor().getPreviousPanelID());
		} else if (e.getActionCommand() == Wizard.NEXT_BUTTON_ACTION_COMMAND) {
			wizard.setCurrentPanel(wizard.getModel().getCurrentDescriptor().getNextPanelID());
		} else if (e.getActionCommand() == Wizard.CANCEL_BUTTON_ACTION_COMMAND) {
			wizard.close(false);
		}
	}
}
