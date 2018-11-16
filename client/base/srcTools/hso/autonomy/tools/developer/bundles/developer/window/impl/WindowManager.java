/*******************************************************************************
 * Copyright 2008, 2012 Hochschule Offenburg
 * Klaus Dorer, Mathias Ehret, Stefan Glaser, Thomas Huber, Fabian Korak,
 * Simon Raffeiner, Srinivasa Ragavan, Thomas Rinklin,
 * Joachim Schilling, Ingo Schindler, Rajit Shahi, Bjoern Weiler
 *
 * This file is part of magmaOffenburg.
 *
 * magmaOffenburg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * magmaOffenburg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with magmaOffenburg. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package hso.autonomy.tools.developer.bundles.developer.window.impl;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import hso.autonomy.tools.developer.bundles.developer.DeveloperProperties;
import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.developer.util.IDeveloperProperties;
import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.developer.bundles.developer.window.Wizards;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;
import hso.autonomy.tools.util.swing.wizard.CompositeWizard;
import hso.autonomy.tools.util.swing.wizard.IWizardDescriptor;

/**
 * The class managing some central windows and wizard instances of the
 * application.
 *
 * @author Stefan Glaser
 */
public class WindowManager implements IWindowManager
{
	private final BundleContext context;

	private MainFrame mainFrame;

	private String mainFrameTitle;

	private Image mainFrameIcon;

	private final List<JFrame> subFrames;

	private final CompositeWizard newWizard;

	private final CompositeWizard runWizard;

	private final CompositeWizard importWizard;

	private final CompositeWizard exportWizard;

	public WindowManager(BundleContext context)
	{
		this.context = context;
		subFrames = new ArrayList<>();

		newWizard = createWizard(Wizards.NEW_WIZARD, IDeveloperBundle.EPT_NEW_WIZARD);
		runWizard = createWizard(Wizards.RUN_WIZARD, IDeveloperBundle.EPT_RUN_WIZARD);
		importWizard = createWizard(Wizards.IMPORT_WIZARD, IDeveloperBundle.EPT_IMPORT_WIZARD);
		exportWizard = createWizard(Wizards.EXPORT_WIZARD, IDeveloperBundle.EPT_EXPORT_WIZARD);

		// Restore preferred wizard indexes
		restorePreferredWizards();
	}

	private CompositeWizard createWizard(Wizards wizard, String extensionPointID)
	{
		List<IWizardDescriptor> subWizards = getSubWizards(extensionPointID);

		if (subWizards.size() > 0) {
			return new CompositeWizard(wizard.dialogTitle, wizard.dialogImage, wizard.title, wizard.icon,
					wizard.description, subWizards, wizard.isModal);
		}

		return null;
	}

	/**
	 * Method to fetch the list of registered wizards to an given extension point
	 * ID.
	 *
	 * @param extensionPointID The ID of the extension point expecting
	 *        {@link IWizardDescriptor} instances
	 * @return the list of registered wizard descriptors
	 */
	private List<IWizardDescriptor> getSubWizards(String extensionPointID)
	{
		List<ExtensionHandle<IWizardDescriptor>> importerHandle =
				context.getExtensions(extensionPointID, IWizardDescriptor.class);
		return importerHandle.stream().map(ExtensionHandle::get).collect(Collectors.toList());
	}

	@Override
	public JFrame getMainWindow()
	{
		if (mainFrame == null) {
			mainFrame = new MainFrame(context, mainFrameTitle, mainFrameIcon);
			mainFrame.refreshMenus();
		}

		return mainFrame;
	}

	@Override
	public void setMainWindowInfo(String windowTitle, Image icon)
	{
		mainFrameTitle = windowTitle;
		mainFrameIcon = icon;
	}

	@Override
	public JFrame createNewWindow()
	{
		JFrame frame;

		// When the main window doesn't exist yet, create that first
		if (mainFrame == null) {
			frame = getMainWindow();
		} else {
			frame = new SubFrame();
			subFrames.add(frame);
		}

		return frame;
	}

	@Override
	public void showWizard(Wizards wizard)
	{
		if (wizard == null) {
			return;
		}

		CompositeWizard wiz = getWizard(wizard);

		if (wiz != null) {
			wiz.showNewWizard(mainFrame);
		}
	}

	@Override
	public List<IWizardDescriptor> getPreferredSubWizards(Wizards wizard)
	{
		CompositeWizard wiz = getWizard(wizard);

		if (wiz != null) {
			// return wiz.getPreviouslyPerformedSubWizards();
			// the amount of subwizards any wizard has is small
			// enough that we can just return all of them for now
			return wiz.getSubWizards();
		}

		return Collections.emptyList();
	}

	private CompositeWizard getWizard(Wizards wizard)
	{
		if (wizard == null) {
			return null;
		}

		switch (wizard) {
		case NEW_WIZARD:
			return newWizard;
		case RUN_WIZARD:
			return runWizard;
		case IMPORT_WIZARD:
			return importWizard;
		case EXPORT_WIZARD:
			return exportWizard;
		default:
			break;
		}

		return null;
	}

	private void storePreferredWizards()
	{
		if (newWizard != null) {
			savePreferredWizard(
					IDeveloperProperties.PREFERRED_NEW_WIZARDS, newWizard.getPreviouslyPerformedSubWizards());
		}
		if (importWizard != null) {
			savePreferredWizard(
					IDeveloperProperties.PREFERRED_IMPORT_WIZARDS, importWizard.getPreviouslyPerformedSubWizards());
		}
		if (exportWizard != null) {
			savePreferredWizard(
					IDeveloperProperties.PREFERRED_EXPORT_WIZARDS, exportWizard.getPreviouslyPerformedSubWizards());
		}
		if (runWizard != null) {
			savePreferredWizard(
					IDeveloperProperties.PREFERRED_RUN_WIZARDS, runWizard.getPreviouslyPerformedSubWizards());
		}
	}

	private void savePreferredWizard(String propertyKey, List<IWizardDescriptor> preferredWizards)
	{
		if (preferredWizards == null || preferredWizards.size() == 0) {
			return;
		}

		String wizardsProp = "";

		for (int i = 0; i < preferredWizards.size(); i++) {
			if (i > 0) {
				wizardsProp += ";";
			}
			wizardsProp += preferredWizards.get(i).getRootWizardPanelID();
		}

		DeveloperProperties.get().setProperty(propertyKey, wizardsProp);
	}

	private void restorePreferredWizards()
	{
		loadPreferredWizardsFor(IDeveloperProperties.PREFERRED_NEW_WIZARDS, newWizard);
		loadPreferredWizardsFor(IDeveloperProperties.PREFERRED_IMPORT_WIZARDS, importWizard);
		loadPreferredWizardsFor(IDeveloperProperties.PREFERRED_EXPORT_WIZARDS, exportWizard);
		loadPreferredWizardsFor(IDeveloperProperties.PREFERRED_RUN_WIZARDS, runWizard);
	}

	private void loadPreferredWizardsFor(String propertyKey, CompositeWizard wizard)
	{
		if (wizard == null) {
			return;
		}

		ArrayList<IWizardDescriptor> preferredWizards = new ArrayList<>();

		String wizardsProp = DeveloperProperties.get().getProperty(propertyKey);
		IWizardDescriptor currentDescriptor;
		if (wizardsProp != null) {
			String[] wizards = wizardsProp.split(";");

			for (String wizard1 : wizards) {
				currentDescriptor = getWizardDescriptor(wizard.getSubWizards(), wizard1);

				if (currentDescriptor != null) {
					preferredWizards.add(currentDescriptor);
				}
			}
		}

		wizard.setPreviouslyPerformedSubWizards(preferredWizards);
	}

	private IWizardDescriptor getWizardDescriptor(List<IWizardDescriptor> wizards, String rootWizardPanelID)
	{
		if (wizards == null || rootWizardPanelID == null || rootWizardPanelID.equals("")) {
			return null;
		}

		for (IWizardDescriptor wizard : wizards) {
			if (wizard.getRootWizardPanelID().equals(rootWizardPanelID)) {
				return wizard;
			}
		}
		return null;
	}

	public void disposeWindows()
	{
		if (mainFrame != null) {
			mainFrame.dispose();
		}

		storePreferredWizards();
	}
}
