package hso.autonomy.tools.util.swing.wizard;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import hso.autonomy.tools.util.swing.DisplayableItemListRenderer;

/**
 * A composite implementation of the wizard class. Multiple
 * {@link IWizardDescriptor} instances are embedded into a single wizard dialog.
 * The so called sub wizards are initially listed in an wizard overview. The
 * user can select and automatically proceed with their wizard of interest.
 *
 * @author Stefan Glaser
 */
public class CompositeWizard
{
	private final String dialogTitle;

	private final Image dialogImage;

	private final String title;

	private final Icon icon;

	private final String description;

	private final List<IWizardDescriptor> subWizards;

	private final List<IWizardDescriptor> previouslyPerformedSubWizards;

	private final boolean modal;

	public CompositeWizard(String dialogTitle, Image dialogImage, String title, Icon icon, String description,
			List<IWizardDescriptor> subWizards, boolean modal)
	{
		this.dialogTitle = dialogTitle;
		this.dialogImage = dialogImage;
		this.title = title;
		this.icon = icon;
		this.description = description;
		this.subWizards = subWizards;
		this.modal = modal;
		this.previouslyPerformedSubWizards = new ArrayList<>();
	}

	public List<IWizardDescriptor> getSubWizards()
	{
		return subWizards;
	}

	public void setPreviouslyPerformedSubWizards(List<IWizardDescriptor> wizards)
	{
		if (wizards == null) {
			return;
		}

		for (IWizardDescriptor wizard : wizards) {
			if (subWizards.contains(wizard)) {
				previouslyPerformedSubWizards.add(wizard);
			}
		}
	}

	public List<IWizardDescriptor> getPreviouslyPerformedSubWizards()
	{
		return previouslyPerformedSubWizards;
	}

	/**
	 * Show a new wizard dialog of this composite wizard type.
	 *
	 * @param owner the owner of the wizard
	 */
	public void showNewWizard(JFrame owner)
	{
		Wizard wizard = new NotifyingCompositeWizard(owner);

		if (modal) {
			wizard.showModalDialog();
		} else {
			wizard.showDialog();
		}
	}

	private void subWizardPerformed(IWizardDescriptor subWizard)
	{
		previouslyPerformedSubWizards.remove(subWizard);
		previouslyPerformedSubWizards.add(0, subWizard);

		if (previouslyPerformedSubWizards.size() > 10) {
			previouslyPerformedSubWizards.remove(10);
		}
	}

	private class NotifyingCompositeWizard extends Wizard
	{
		private final SubWizardSelectorPanelDescriptor subWizardSelectorPanel;

		public NotifyingCompositeWizard(JFrame owner)
		{
			super(owner);

			setDialogTitle(dialogTitle);
			setDialogIconImage(dialogImage);
			setTitle(title);
			setDescription(description);
			setIcon(icon);

			// Register root wizard panel
			subWizardSelectorPanel = new SubWizardSelectorPanelDescriptor();
			registerWizardPanel(subWizardSelectorPanel);

			rootWizardPanelID = subWizardSelectorPanel.getID();

			// Register importer wizard panels
			IWizardDescriptor currentDescriptor;
			IWizardPanelDescriptor[] wizardPanels;
			for (IWizardDescriptor subWizard : subWizards) {
				currentDescriptor = subWizard;
				wizardPanels = currentDescriptor.createWizardPanels();

				wizardPanels[0].setPreviousPanelID(rootWizardPanelID);

				for (IWizardPanelDescriptor wizardPanel : wizardPanels) {
					registerWizardPanel(wizardPanel);
				}
			}
		}

		@Override
		protected void onClose(boolean success)
		{
			super.onClose(success);

			IWizardDescriptor selectedSubWizard = subWizardSelectorPanel.getSelectedSubWizard();

			if (selectedSubWizard != null) {
				subWizardPerformed(selectedSubWizard);
			}
		}
	}

	protected class SubWizardSelectorPanelDescriptor extends WizardPanelDescriptorBase
	{
		private JList<IWizardDescriptor> subWizardsList;

		public SubWizardSelectorPanelDescriptor()
		{
			super("compositeWizard.root", null, null, null);
		}

		@Override
		protected JComponent createPanelComponent()
		{
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BorderLayout());

			DefaultListModel<IWizardDescriptor> importerListModel = new DefaultListModel<>();
			subWizards.forEach(importerListModel::addElement);
			subWizardsList = new JList<>(importerListModel);
			subWizardsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			subWizardsList.setCellRenderer(new DisplayableItemListRenderer());
			subWizardsList.addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting()) {
					IWizardDescriptor selectedSubWizard = subWizardsList.getSelectedValue();
					nextID = selectedSubWizard.getRootWizardPanelID();
					wizard.setTitle(selectedSubWizard.getTitle());
					wizard.setDescription(selectedSubWizard.getDescription());
					setNextButtonEnabled(true);
				}
			});
			subWizardsList.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1 && nextID != null) {
						wizard.setCurrentPanel(nextID);
					}
				}
			});
			contentPanel.add(new JScrollPane(subWizardsList), BorderLayout.CENTER);

			return contentPanel;
		}

		@Override
		public void onDisplayPanel()
		{
			super.onDisplayPanel();

			if (!subWizardsList.isSelectionEmpty()) {
				IWizardDescriptor selectedSubWizard = subWizardsList.getSelectedValue();
				wizard.setTitle(selectedSubWizard.getTitle());
				wizard.setDescription(selectedSubWizard.getDescription());
			} else {
				wizard.setTitle(title);
				wizard.setDescription(description);
			}
		}

		public IWizardDescriptor getSelectedSubWizard()
		{
			if (subWizardsList != null && subWizardsList.getSelectedValue() != null) {
				return subWizardsList.getSelectedValue();
			}

			return null;
		}
	}
}
