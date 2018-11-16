package hso.autonomy.tools.util.swing.wizard;

import javax.swing.KeyStroke;

import hso.autonomy.tools.util.swing.model.IDisplayableItem;

/**
 * Descriptor to pack a single, but multiple-panel wizard.
 *
 * @author Stefan Glaser
 */
public interface IWizardDescriptor extends IDisplayableItem {
	/**
	 * The name of the group this wizard belongs to. This information may be used
	 * to group this wizard among others in a wizard selection.
	 *
	 * @return the wizard group name
	 */
	String getGroupName();

	/**
	 * Retrieve the ID of the root wizard panel.
	 *
	 * @return the root panel index
	 */
	String getRootWizardPanelID();

	/**
	 * Retrieve an new array of the wizard panel descriptors of this wizard. <br>
	 * <br>
	 * <b>Note:</b>The first wizard panel descriptor is interpreted as / expected
	 * to be the root panel descriptor!
	 *
	 * @return an new array of wizard panel descriptors, beginning with the root
	 *         panel descriptor
	 */
	IWizardPanelDescriptor[] createWizardPanels();

	/**
	 * Keyboard shortcut that can be used to open this wizard.
	 */
	KeyStroke getAccelerator();
}
