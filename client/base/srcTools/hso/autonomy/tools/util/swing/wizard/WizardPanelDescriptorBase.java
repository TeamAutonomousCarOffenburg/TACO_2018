package hso.autonomy.tools.util.swing.wizard;

import javax.swing.Action;
import javax.swing.JComponent;

/**
 * @author Stefan Glaser
 */
public abstract class WizardPanelDescriptorBase implements IWizardPanelDescriptor
{
	protected Wizard wizard;

	protected JComponent panelComponent;

	protected String id;

	protected String previousID;

	protected String nextID;

	protected Action finishAction;

	public WizardPanelDescriptorBase(String id, String previousID, String nextID, Action finishAction)
	{
		this.id = id;
		this.previousID = previousID;
		this.nextID = nextID;
		this.finishAction = finishAction;
	}

	protected abstract JComponent createPanelComponent();

	protected void setBackButtonEnabled(boolean enabled)
	{
		wizard.getModel().setBackButtonEnabled(enabled);
	}

	protected void setNextButtonEnabled(boolean enabled)
	{
		wizard.getModel().setNextButtonEnabled(enabled);
	}

	@Override
	public JComponent getPanelComponent()
	{
		if (panelComponent == null) {
			panelComponent = createPanelComponent();
		}

		return panelComponent;
	}

	@Override
	public String getID()
	{
		return id;
	}

	@Override
	public void setPreviousPanelID(String previousID)
	{
		this.previousID = previousID;
	}

	@Override
	public String getPreviousPanelID()
	{
		return previousID;
	}

	@Override
	public String getNextPanelID()
	{
		return nextID;
	}

	@Override
	public Action getFinishAction()
	{
		return finishAction;
	}

	@Override
	public void setWizard(Wizard wizard)
	{
		this.wizard = wizard;
	}

	@Override
	public void onDisplayPanel()
	{
		wizard.getModel().setBackButtonEnabled(previousID != null);
		wizard.getModel().setNextButtonEnabled(nextID != null);
	}

	@Override
	public void onHidePanel()
	{
	}
}
