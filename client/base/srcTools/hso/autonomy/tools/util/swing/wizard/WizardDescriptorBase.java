package hso.autonomy.tools.util.swing.wizard;

import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * A default implementation for a wizard descriptor.
 *
 * @author Stefan Glaser
 */
public abstract class WizardDescriptorBase implements IWizardDescriptor
{
	protected final Icon icon;

	protected final String title;

	protected final String description;

	protected final String groupName;

	protected final String rootWizardPanelID;

	public WizardDescriptorBase(Icon icon, String title, String description, String groupName, String rootWizardPanelID)
	{
		this.icon = icon;
		this.title = title;
		this.description = description;
		this.groupName = groupName;
		this.rootWizardPanelID = rootWizardPanelID;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getToolTip()
	{
		return description;
	}

	@Override
	public String getGroupName()
	{
		return groupName;
	}

	@Override
	public String getRootWizardPanelID()
	{
		return rootWizardPanelID;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj)) {
			return true;
		}

		if (obj instanceof WizardDescriptorBase) {
			WizardDescriptorBase other = (WizardDescriptorBase) obj;
			if (title.equals(other.title) && description.equals(other.description) &&
					groupName.equals(other.groupName) && rootWizardPanelID.equals(other.rootWizardPanelID)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public KeyStroke getAccelerator()
	{
		return null;
	}
}
