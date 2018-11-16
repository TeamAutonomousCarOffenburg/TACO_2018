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
package hso.autonomy.tools.developer.bundles.developer.perspective;

import hso.autonomy.tools.util.swing.command.ICommandList;

import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JComponent;

public interface IView {
	String PROPERTY_NAME = "view.name";

	IViewDescriptor getDescriptor();

	String getName();

	void setName(String name);

	void setIndex(int index);

	JComponent getContentComponent();

	JComponent getInnerContentComponent();

	boolean wasModified();

	Action getSaveAction();

	Action getSaveAsAction();

	void discardChanges();

	boolean acceptsObject(Object o);

	void selectObject(Object o);

	Object getSelectedObject();

	ICommandList getCommandList();

	void addPropertyChangeListener(PropertyChangeListener listener);

	void removePropertyChangeListener(PropertyChangeListener listener);

	boolean isClosable();

	void onClose();
}
