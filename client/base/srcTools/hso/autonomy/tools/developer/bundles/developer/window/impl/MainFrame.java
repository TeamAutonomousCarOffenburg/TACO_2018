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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import hso.autonomy.tools.developer.bundles.developer.DeveloperProperties;
import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.util.IDeveloperProperties;
import hso.autonomy.tools.developer.bundles.developer.window.impl.menu.ExitAction;
import hso.autonomy.tools.developer.bundles.developer.window.statusBar.IStatusObserver;
import hso.autonomy.tools.developer.bundles.developer.window.statusBar.impl.StatusBar;
import hso.autonomy.tools.developer.bundles.developer.window.impl.menu.MainMenuBar;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;
import hso.autonomy.tools.util.imageBuffer.ImageFile;

public class MainFrame extends JFrame
{
	private final BundleContext context;

	private final Image icon;

	private final String windowTitle;

	private final IPerspectiveManager perspectiveManager;

	private StatusBar statusBar;

	private MainMenuBar mainMenuBar;

	public MainFrame(BundleContext context, String windowTitle, Image icon)
	{
		this.context = context;
		this.windowTitle = windowTitle;
		this.icon = icon;

		perspectiveManager = IDeveloperService.PERSPECTIVE_MANAGER.get(context);

		initializeComponents();
		restoreProperties();
		initializeListeners();
	}

	private void initializeComponents()
	{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setIconImage(icon);

		// Set ContentPane Settings
		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(new Color(128, 128, 128));

		setTitle(windowTitle);

		// Create mainMenuBar
		mainMenuBar = new MainMenuBar(context);

		// Create Status Bar
		List<IStatusObserver> statusObserver = new ArrayList<>();
		List<ExtensionHandle<IStatusObserver>> handles =
				context.getExtensions(IDeveloperBundle.EPT_STATUS_OBSERVER, IStatusObserver.class);

		statusObserver.addAll(handles.stream().map(ExtensionHandle::get).collect(Collectors.toList()));
		statusBar = new StatusBar(statusObserver);

		// Add Components
		setJMenuBar(mainMenuBar);
		getContentPane().add(perspectiveManager.createLeftViewBarContainer(), BorderLayout.WEST);
		getContentPane().add(perspectiveManager.createViewSplitContainer(), BorderLayout.CENTER);
		getContentPane().add(statusBar, BorderLayout.PAGE_END);
	}

	private void restoreProperties()
	{
		restoreSize();
		restoreLocation();
		restoreState();
	}

	private void restoreSize()
	{
		int width = DeveloperProperties.get().getProperty(IDeveloperProperties.MAIN_WINDOW_WIDTH, 1024);
		int height = DeveloperProperties.get().getProperty(IDeveloperProperties.MAIN_WINDOW_HEIGHT, 768);

		setSize(width, height);
	}

	private void restoreLocation()
	{
		int x = DeveloperProperties.get().getProperty(IDeveloperProperties.MAIN_WINDOW_X, Integer.MIN_VALUE);
		int y = DeveloperProperties.get().getProperty(IDeveloperProperties.MAIN_WINDOW_Y, Integer.MIN_VALUE);

		if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
			setLocationRelativeTo(null);
		} else {
			setLocation(x, y);
		}
	}

	private void restoreState()
	{
		boolean maximized = DeveloperProperties.get().getProperty(IDeveloperProperties.MAIN_WINDOW_MAXIMIZED, false);
		setState(maximized ? Frame.MAXIMIZED_BOTH : Frame.NORMAL);
	}

	private void initializeListeners()
	{
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				new ExitAction(context).actionPerformed(null);
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e)
			{
				DeveloperProperties.get().setProperty(IDeveloperProperties.MAIN_WINDOW_WIDTH, getWidth());
				DeveloperProperties.get().setProperty(IDeveloperProperties.MAIN_WINDOW_HEIGHT, getHeight());
				context.saveBundleProperties();
			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
				DeveloperProperties.get().setProperty(IDeveloperProperties.MAIN_WINDOW_X, getX());
				DeveloperProperties.get().setProperty(IDeveloperProperties.MAIN_WINDOW_Y, getY());
				context.saveBundleProperties();
			}
		});

		addWindowStateListener(e -> {
			boolean maximized = (getState() & Frame.MAXIMIZED_BOTH) > 0;
			DeveloperProperties.get().setProperty(IDeveloperProperties.MAIN_WINDOW_MAXIMIZED, maximized);
			context.saveBundleProperties();
		});
	}

	/**
	 * Refreshes the main menu, mostly to make sure keyboard shortcuts are
	 * initialized.
	 */
	public void refreshMenus()
	{
		mainMenuBar.refreshMenus();
	}
}
