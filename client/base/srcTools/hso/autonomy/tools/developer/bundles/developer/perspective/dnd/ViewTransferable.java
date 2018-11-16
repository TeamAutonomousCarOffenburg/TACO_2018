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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewContainer;

public class ViewTransferable implements Transferable
{
	private ViewTransferData data = null;

	public static final String DATA_FLAVOR_NAME = "ViewTransferData";

	public static final DataFlavor DATA_FLAVOR =
			new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, DATA_FLAVOR_NAME);

	public ViewTransferable(IViewContainer container, IView view, int index)
	{
		data = new ViewTransferData(container, view, index);
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
	{
		return data;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] {DATA_FLAVOR};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor.getHumanPresentableName().equals(DATA_FLAVOR_NAME);
	}
}
