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

import java.util.List;

import hso.autonomy.tools.developer.bundles.developer.perspective.impl.ViewBarContainer;
import hso.autonomy.tools.developer.bundles.developer.perspective.impl.ViewSplitContainer;

/**
 * @author Stefan Glaser
 */
public interface IPerspectiveManager {
	/** Returns all views that don't require an Object to be opened */
	List<IViewDescriptor> getAvailableViews();

	/** Returns all views that accept the provided Object */
	List<IViewDescriptor> getAvailableViews(Object o);

	IViewDescriptor getView(String viewID);

	List<IView> getRegisteredViews();

	void setActiveView(IView view);

	IView getActiveView();

	IView openView(IViewDescriptor viewDescriptor, Object o);

	IView openView(IViewDescriptor viewDescriptor);

	IView openView(IViewDescriptor viewDescriptor, IViewContainer container, Object o);

	IView openView(String viewID, IViewContainer container);

	IView openView(String viewID, IViewContainer container, Object o);

	IView openView(Object o, IViewContainer container);

	IView openView(Object o);

	boolean closeView(IView view);

	ViewSplitContainer createViewSplitContainer();

	ViewBarContainer createLeftViewBarContainer();

	boolean removeViewSplitContainer(ViewSplitContainer container);

	boolean onWindowClosing();

	void addPerspectiveManagerListener(IPerspectiveManagerListener listener);

	void removePerspectiveManagerListener(IPerspectiveManagerListener listener);
}
