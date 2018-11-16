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
package hso.autonomy.tools.util.imageBuffer;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImageLoader
{
	private static final String IMAGES_BASE_PATH = "/images/";

	private static final Map<String, Icon> iconMap = new HashMap<>();

	private static Icon brokenIcon;

	public static Image getImage(String iconKey)
	{
		// Try to fetch buffered image
		Icon icon = getIcon(iconKey);

		if (icon != null) {
			return ((ImageIcon) icon).getImage();
		}

		return null;
	}

	static Icon getIcon(String iconKey)
	{
		// Try to fetch buffered image
		Icon icon = iconMap.get(iconKey);

		if (icon == null) {
			// Lazy load specified image, if it is not already in the imagesMap
			icon = loadIcon(iconKey);
			if (icon != null) {
				iconMap.put(iconKey, icon);
			} else {
				// If loading the icon failed, a defaultIcon should be returned
				if (brokenIcon == null) {
					brokenIcon = ImageFile.BROKEN_IMAGE.getIcon();
				}
				icon = brokenIcon;
			}
		}

		return icon;
	}

	static Icon loadIcon(String iconPath)
	{
		if (iconPath == null) {
			return null;
		}

		ImageIcon icon = null;

		try {
			icon = new ImageIcon(ImageLoader.class.getResource(IMAGES_BASE_PATH + iconPath));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return icon;
	}
}
