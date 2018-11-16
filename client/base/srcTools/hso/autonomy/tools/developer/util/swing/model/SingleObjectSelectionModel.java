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
package hso.autonomy.tools.developer.util.swing.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;

public class SingleObjectSelectionModel<T>
{
	private T selectedItem;

	private List<ChangeListener> listeners;

	public SingleObjectSelectionModel()
	{
		listeners = new ArrayList<>();
	}

	public void setSelectedItem(T newItem)
	{
		if (selectedItem != newItem) {
			selectedItem = newItem;
			publishSelectedItemChanged();
		}
	}

	public T getSelectedItem()
	{
		return selectedItem;
	}

	protected void publishSelectedItemChanged()
	{
		for (ChangeListener l : listeners) {
			l.stateChanged(null);
		}
	}

	public boolean addListener(ChangeListener l)
	{
		return listeners.add(l);
	}

	public boolean removeListener(ChangeListener l)
	{
		return listeners.remove(l);
	}

	public void clearSelection()
	{
		setSelectedItem(null);
	}
}
