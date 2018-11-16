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

public class PerspectiveManagerConfig
{
	private final List<String> leftBarViews;

	private final int leftBarActiveView;

	private final List<ViewSplitContainerConfig> splitContainerConfigs;

	public PerspectiveManagerConfig(
			List<String> leftBarViews, int leftBarActiveView, List<ViewSplitContainerConfig> splitContainerConfigs)
	{
		this.leftBarViews = leftBarViews;
		this.leftBarActiveView = leftBarActiveView;
		this.splitContainerConfigs = splitContainerConfigs;
	}

	public List<String> getLeftBarViews()
	{
		return leftBarViews;
	}

	public int getLeftBarActiveView()
	{
		return leftBarActiveView;
	}

	public List<ViewSplitContainerConfig> getSplitContainerConfigs()
	{
		return splitContainerConfigs;
	}
}
