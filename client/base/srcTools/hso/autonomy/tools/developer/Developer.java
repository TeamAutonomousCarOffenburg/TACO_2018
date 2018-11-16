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
package hso.autonomy.tools.developer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.util.bundleFramework.BundleManager;
import hso.autonomy.tools.util.bundleFramework.service.ServiceHandle;

import java.awt.Image;
import java.util.Locale;

public class Developer
{
	public static void startDeveloper(String name, Image icon, String bundlePath)
	{
		// Service based version
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException |
				 ClassNotFoundException e) {
		}

		// Create BundleManager
		BundleManager bundleManager = new BundleManager(System.getProperty("user.home") + "/." + name);

		// Load bundles
		bundleManager.loadBundles("hso/autonomy/tools/developer/bundles/");
		if (bundlePath != null) {
			bundleManager.loadBundles(bundlePath);
		}

		Locale.setDefault(Locale.US);

		// run application
		IWindowManager windowManager =
				bundleManager.getService(IDeveloperBundle.SRV_WINDOW_MANAGER, IWindowManager.class).get();
		windowManager.setMainWindowInfo(name, icon);
		JFrame frame = windowManager.createNewWindow();
		frame.setVisible(true);
	}
}
