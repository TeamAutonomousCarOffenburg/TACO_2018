package hso.autonomy.tools.util.swing.wizard;

import javax.swing.Action;
import javax.swing.JComponent;

/**
 * @author Stefan Glaser
 */
public interface IWizardPanelDescriptor {
	JComponent getPanelComponent();

	String getID();

	Action getFinishAction();

	void setPreviousPanelID(String previousID);

	String getPreviousPanelID();

	String getNextPanelID();

	void setWizard(Wizard wizard);

	void onHidePanel();

	void onDisplayPanel();
}
