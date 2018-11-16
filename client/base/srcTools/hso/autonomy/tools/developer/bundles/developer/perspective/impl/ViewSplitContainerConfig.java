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
package hso.autonomy.tools.developer.bundles.developer.perspective.impl;

import java.util.List;

public class ViewSplitContainerConfig
{
	private List<OpenViewConfig> firstContainerViews;

	private List<OpenViewConfig> secondContainerViews;

	private ViewSplitContainerConfig firstSplitContainerConfig;

	private ViewSplitContainerConfig secondSplitContainerConfig;

	private int dividerLocation;

	private int orientation;

	public ViewSplitContainerConfig(List<OpenViewConfig> firstContainerViews, List<OpenViewConfig> secondContainerViews,
			ViewSplitContainerConfig firstSplitContainerConfig, ViewSplitContainerConfig secondSplitContainerConfig,
			int dividerLocation, int orientation)
	{
		this.firstContainerViews = firstContainerViews;
		this.secondContainerViews = secondContainerViews;
		this.firstSplitContainerConfig = firstSplitContainerConfig;
		this.secondSplitContainerConfig = secondSplitContainerConfig;
		this.dividerLocation = dividerLocation;
		this.orientation = orientation;
	}

	public int getDividerLocation()
	{
		return dividerLocation;
	}

	public int getOrientation()
	{
		return orientation;
	}

	public List<OpenViewConfig> getFirstContainerViews()
	{
		return firstContainerViews;
	}

	public List<OpenViewConfig> getSecondContainerViews()
	{
		return secondContainerViews;
	}

	public ViewSplitContainerConfig getFirstSplitContainerConfig()
	{
		return firstSplitContainerConfig;
	}

	public ViewSplitContainerConfig getSecondSplitContainerConfig()
	{
		return secondSplitContainerConfig;
	}
}
