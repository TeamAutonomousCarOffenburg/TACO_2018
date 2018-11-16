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
package hso.autonomy.tools.developer.bundles.developer.window.impl.menu;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;

public class MainMenuBar extends JMenuBar
{
	private final BundleContext context;

	private FileMenu fileMenu;

	private RunMenu runMenu;

	public MainMenuBar(BundleContext context)
	{
		this.context = context;

		initializeComponents();
	}

	private void initializeComponents()
	{
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		add(fileMenu = new FileMenu(context));
		add(new EditMenu(context));
		add(runMenu = new RunMenu(context));
		add(new ViewMenu(context));

		List<ExtensionHandle<JMenu>> menuExtensions = context.getExtensions(IDeveloperBundle.EPT_MENU, JMenu.class);
		for (ExtensionHandle<JMenu> extension : menuExtensions) {
			add(extension.get());
		}

		JPopupMenu.setDefaultLightWeightPopupEnabled(true);
	}

	public void refreshMenus()
	{
		fileMenu.refreshMenus();
		runMenu.refreshMenus();
	}
}
