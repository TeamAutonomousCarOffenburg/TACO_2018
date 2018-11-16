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

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewDescriptor;
import hso.autonomy.tools.developer.util.perspective.ViewSelectorDialog;
import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.util.bundleFramework.BundleContext;

public class ViewMenu extends JMenu
{
	private BundleContext context;

	private IPerspectiveManager perspectiveManager;

	private IWindowManager windowManager;

	public ViewMenu(BundleContext context)
	{
		this.context = context;

		initializeComponents();
	}

	private void initializeComponents()
	{
		setText("View");
		setName("ViewMenu");
		setMnemonic(KeyEvent.VK_V);

		perspectiveManager = IDeveloperService.PERSPECTIVE_MANAGER.get(context);
		windowManager = IDeveloperService.WINDOW_MANAGER.get(context);

		JMenuItem menuItem;
		for (IViewDescriptor view : perspectiveManager.getAvailableViews()) {
			menuItem = new JMenuItem(new OpenViewAction(view));
			add(menuItem);
		}
		addSeparator();

		JMenuItem openViewMenuItem = new JMenuItem(new OpenViewSelectorAction());
		openViewMenuItem.setMnemonic(KeyEvent.VK_O);
		openViewMenuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		add(openViewMenuItem);
	}

	private class OpenViewAction extends AbstractAction
	{
		private final IViewDescriptor view;

		public OpenViewAction(IViewDescriptor view)
		{
			super(view.getTitle(), view.getIcon());
			this.view = view;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			perspectiveManager.openView(view);
		}
	}

	private class OpenViewSelectorAction extends AbstractAction
	{
		private ViewSelectorDialog dlg;

		public OpenViewSelectorAction()
		{
			super("Open View");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (dlg == null) {
				dlg = new ViewSelectorDialog(perspectiveManager, windowManager);
			}

			dlg.setLocationRelativeTo(windowManager.getMainWindow());
			dlg.setVisible(true);
		}
	}
}
