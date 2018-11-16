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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JSplitPane;

import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewContainer;

public class ViewSplitContainer extends JSplitPane implements IViewContainer
{
	private static int splitContainerCnt = 1;

	private static int containerCnt = 1;

	private IPerspectiveManager perspectiveManager;

	private ViewSplitContainer parent;

	private IViewContainer firstTabContainer;

	private IViewContainer secondTabContainer;

	private String containerID;

	public enum Placement { NORTH, EAST, SOUTH, WEST }

	public ViewSplitContainer(IPerspectiveManager perspectiveManager)
	{
		this(perspectiveManager, null, null, null, JSplitPane.HORIZONTAL_SPLIT);
	}

	protected ViewSplitContainer(IPerspectiveManager perspectiveManager, ViewSplitContainer parent,
			IViewContainer firstContainer, IViewContainer secondContainer, int orientation)
	{
		super(orientation);
		this.perspectiveManager = perspectiveManager;
		this.parent = parent;
		this.firstTabContainer = firstContainer;
		this.secondTabContainer = secondContainer;
		containerID = "SplitContainer_" + splitContainerCnt++;

		initializeComponents();
	}

	protected ViewSplitContainer(
			IPerspectiveManager perspectiveManager, ViewSplitContainer parent, ViewSplitContainerConfig config)
	{
		super(config.getOrientation());
		this.perspectiveManager = perspectiveManager;
		this.parent = parent;
		containerID = "SplitContainer_" + splitContainerCnt++;

		ViewSplitContainerConfig firstConfig = config.getFirstSplitContainerConfig();
		ViewSplitContainerConfig secondConfig = config.getSecondSplitContainerConfig();

		try {
			restoreConfig(config, firstConfig, secondConfig);
		} catch (Exception e) {
			System.err.println("Failed to restore ViewSplitContainer config.");
			e.printStackTrace();
		}

		initializeComponents();
		setDividerLocation(config.getDividerLocation());
	}

	private void restoreConfig(ViewSplitContainerConfig config, ViewSplitContainerConfig firstConfig,
			ViewSplitContainerConfig secondConfig)
	{
		if (firstConfig != null) {
			firstTabContainer = new ViewSplitContainer(perspectiveManager, this, firstConfig);
		} else {
			List<OpenViewConfig> openViews = config.getFirstContainerViews();
			firstTabContainer = createTabContainer();

			for (OpenViewConfig openView : openViews) {
				perspectiveManager.openView(openView.getID(), firstTabContainer, openView.getSelectedObject());
			}
		}

		if (secondConfig != null) {
			secondTabContainer = new ViewSplitContainer(perspectiveManager, this, secondConfig);
		} else {
			List<OpenViewConfig> openViews = config.getSecondContainerViews();
			secondTabContainer = createTabContainer();

			for (OpenViewConfig openView : openViews) {
				perspectiveManager.openView(openView.getID(), secondTabContainer, openView.getSelectedObject());
			}
		}
	}

	private void initializeComponents()
	{
		setDividerSize(8);
		setOneTouchExpandable(true);
		setResizeWeight(1);

		if (firstTabContainer == null) {
			firstTabContainer = createTabContainer();
		}

		if (secondTabContainer == null) {
			secondTabContainer = createTabContainer();
		}

		firstTabContainer.setParent(this);
		secondTabContainer.setParent(this);

		setLeftComponent((Component) firstTabContainer);
		setRightComponent((Component) secondTabContainer);
	}

	private ViewTabContainer createTabContainer()
	{
		return new ViewTabContainer(perspectiveManager, "Container_" + containerCnt++, this);
	}

	@Override
	public String getContainerID()
	{
		return containerID;
	}

	@Override
	public void setParent(ViewSplitContainer parent)
	{
		this.parent = parent;
	}

	public void setFirstTabContainer(IViewContainer firstContainer)
	{
		firstTabContainer = firstContainer;
		firstTabContainer.setParent(this);
		setLeftComponent((Component) firstTabContainer);
	}

	public void setSecondTabContainer(IViewContainer secondContainer)
	{
		secondTabContainer = secondContainer;
		secondTabContainer.setParent(this);
		setRightComponent((Component) secondTabContainer);
	}

	@Override
	public void addView(IView view)
	{
		firstTabContainer.addView(view);
	}

	@Override
	public boolean removeView(IView view)
	{
		if (!firstTabContainer.removeView(view)) {
			return secondTabContainer.removeView(view);
		}
		return true;
	}

	@Override
	public List<IView> getOpenViews()
	{
		return null;
	}

	public void splitContainer(IViewContainer source, IView newView, Placement newViewPlacement)
	{
		if (firstTabContainer != source && secondTabContainer != source) {
			return;
		}

		int dividerLoc = getDividerLocation();
		int newSplitDividerLoc = ((Component) source).getWidth() / 2;

		// Remove source container
		remove((Component) source);

		// Create new tab container
		IViewContainer newTabContainer = new ViewTabContainer(perspectiveManager, "Container_" + containerCnt++, null);

		// Create new split container and add source and the new tab container to
		// it
		ViewSplitContainer newSplitContainer;
		switch (newViewPlacement) {
		case NORTH:
			newSplitContainer = new ViewSplitContainer(
					perspectiveManager, this, newTabContainer, source, JSplitPane.VERTICAL_SPLIT);
			newSplitDividerLoc = ((Component) source).getHeight() / 2;
			break;
		case EAST:
			newSplitContainer = new ViewSplitContainer(
					perspectiveManager, this, source, newTabContainer, JSplitPane.HORIZONTAL_SPLIT);
			break;
		case SOUTH:
			newSplitContainer = new ViewSplitContainer(
					perspectiveManager, this, source, newTabContainer, JSplitPane.VERTICAL_SPLIT);
			newSplitDividerLoc = ((Component) source).getHeight() / 2;
			break;
		case WEST:
		default:
			newSplitContainer = new ViewSplitContainer(
					perspectiveManager, this, newTabContainer, source, JSplitPane.HORIZONTAL_SPLIT);
			break;
		}
		// Set the parent of the new tab container
		newTabContainer.setParent(newSplitContainer);
		newTabContainer.addView(newView);

		// Set the new split container as the previous source
		if (firstTabContainer == source) {
			setFirstTabContainer(newSplitContainer);
		} else if (secondTabContainer == source) {
			setSecondTabContainer(newSplitContainer);
		}
		newSplitContainer.setDividerLocation(newSplitDividerLoc);
		setDividerLocation(dividerLoc);
	}

	public void splitParentContainer(IViewContainer source, IView newView, Placement newViewPlacement)
	{
		if (firstTabContainer != source && secondTabContainer != source) {
			return;
		}

		if (parent != null) {
			parent.splitContainer(this, newView, newViewPlacement);
		} else {
			splitContainer(source, newView, newViewPlacement);
		}
	}

	public void removeViewContainer(IViewContainer container)
	{
		if (parent != null) {
			if (container == firstTabContainer) {
				removeAll();
				parent.replaceComponent(this, secondTabContainer);
			} else if (container == secondTabContainer) {
				removeAll();
				parent.replaceComponent(this, firstTabContainer);
			}
		}
	}

	public void replaceComponent(ViewSplitContainer containerToReplace, IViewContainer replacement)
	{
		int dividerLoc = getDividerLocation();
		if (firstTabContainer == containerToReplace) {
			remove(containerToReplace);
			setFirstTabContainer(replacement);
			setDividerLocation(dividerLoc);
		} else if (secondTabContainer == containerToReplace) {
			remove(containerToReplace);
			setSecondTabContainer(replacement);
			setDividerLocation(dividerLoc);
		}
	}

	public ViewSplitContainerConfig getConfig()
	{
		List<OpenViewConfig> firstContainerViews = new ArrayList<>();
		List<OpenViewConfig> secondContainerViews = new ArrayList<>();

		ViewSplitContainerConfig firstSplitContainerConfig = null;
		ViewSplitContainerConfig secondSplitContainerConfig = null;

		if (firstTabContainer instanceof ViewSplitContainer) {
			firstSplitContainerConfig = ((ViewSplitContainer) firstTabContainer).getConfig();
		} else {
			for (IView view : firstTabContainer.getOpenViews()) {
				firstContainerViews.add(new OpenViewConfig(view.getDescriptor().getViewID(), view.getSelectedObject()));
			}
		}

		if (secondTabContainer instanceof ViewSplitContainer) {
			secondSplitContainerConfig = ((ViewSplitContainer) secondTabContainer).getConfig();
		} else {
			for (IView view : secondTabContainer.getOpenViews()) {
				secondContainerViews.add(
						new OpenViewConfig(view.getDescriptor().getViewID(), view.getSelectedObject()));
			}
		}

		return new ViewSplitContainerConfig(firstContainerViews, secondContainerViews, firstSplitContainerConfig,
				secondSplitContainerConfig, getDividerLocation(), getOrientation());
	}
}
