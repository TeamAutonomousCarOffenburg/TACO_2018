package hso.autonomy.tools.util.swing.wizard;

import java.util.HashMap;

import hso.autonomy.tools.util.swing.model.ModelSupport;

/**
 * @author Stefan Glaser
 */
public class WizardModel extends ModelSupport
{
	public static final String PROPERTY_CURRENT_DESCRIPTOR = "wizard.currentDescriptor";

	public static final String PROPERTY_BACK_BUTTON_ENABLED = "wizard.backButtonEnabled";

	public static final String PROPERTY_NEXT_BUTTON_ENABLED = "wizard.nextButtonEnabled";

	private final HashMap<String, IWizardPanelDescriptor> descriptors;

	private IWizardPanelDescriptor currentDescriptor;

	private boolean backBtnEnabled = true;

	private boolean nextBtnEnabled = true;

	public WizardModel()
	{
		descriptors = new HashMap<>();
	}

	void registerPanelDescriptor(IWizardPanelDescriptor descriptor)
	{
		descriptors.put(descriptor.getID(), descriptor);
	}

	public void setCurrentDescriptor(String id)
	{
		if (!descriptors.containsKey(id)) {
			throw new IllegalArgumentException("Descriptor \"" + id + "\" not found!");
		}

		if (currentDescriptor == null || !currentDescriptor.getID().equals(id)) {
			IWizardPanelDescriptor oldValue = currentDescriptor;
			currentDescriptor = descriptors.get(id);
			changeSupport().firePropertyChange(PROPERTY_CURRENT_DESCRIPTOR, oldValue, currentDescriptor);

			setBackButtonEnabled(currentDescriptor.getPreviousPanelID() != null);
		}
	}

	public IWizardPanelDescriptor getCurrentDescriptor()
	{
		return currentDescriptor;
	}

	public boolean isBackButtonEnabled()
	{
		return backBtnEnabled;
	}

	public boolean isNextButtonEnabled()
	{
		return nextBtnEnabled;
	}

	public void setBackButtonEnabled(boolean enabled)
	{
		if (backBtnEnabled != enabled) {
			boolean oldValue = backBtnEnabled;
			backBtnEnabled = enabled;
			changeSupport().firePropertyChange(PROPERTY_BACK_BUTTON_ENABLED, oldValue, backBtnEnabled);
		}
	}

	public void setNextButtonEnabled(boolean enabled)
	{
		if (nextBtnEnabled != enabled) {
			boolean oldValue = nextBtnEnabled;
			nextBtnEnabled = enabled;
			changeSupport().firePropertyChange(PROPERTY_NEXT_BUTTON_ENABLED, oldValue, nextBtnEnabled);
		}
	}
}
