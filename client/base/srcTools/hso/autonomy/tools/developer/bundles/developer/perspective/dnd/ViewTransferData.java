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
package hso.autonomy.tools.developer.bundles.developer.perspective.dnd;

import hso.autonomy.tools.developer.bundles.developer.perspective.IViewContainer;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;

public class ViewTransferData
{
	private IViewContainer sourceContainer;

	private IView view;

	private int index = -1;

	public ViewTransferData()
	{
	}

	public ViewTransferData(IViewContainer sourceContainer, IView view, int tabIndex)
	{
		this.sourceContainer = sourceContainer;
		this.view = view;
		index = tabIndex;
	}

	public IViewContainer getSourceContainer()
	{
		return sourceContainer;
	}

	public void setSourceContainer(IViewContainer pane)
	{
		sourceContainer = pane;
	}

	public IView getView()
	{
		return view;
	}

	public void setView(IView view)
	{
		this.view = view;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}
}
