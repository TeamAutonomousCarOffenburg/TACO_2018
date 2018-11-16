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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewContainer;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewDescriptor;
import org.apache.commons.math3.util.Pair;

import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManagerListener;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;

public class PerspectiveManager implements IPerspectiveManager
{
	private static final String CONFIG_PATH = "perspectiveManager.cfg";

	private PerspectiveManagerConfig config;

	private static int containerCount = 0;

	private BundleContext context;

	private List<IPerspectiveManagerListener> listeners;

	private final Map<String, ViewSplitContainer> viewSplitContainer;

	private ViewBarContainer viewBarContainer;

	private final List<IView> openViews;

	private final List<IViewDescriptor> availableViews;

	private IView activeView;

	private boolean isViewSplitContainerCreated = false;

	private boolean isLeftViewBarContainerCreated = false;

	public PerspectiveManager(BundleContext context)
	{
		this.context = context;
		viewSplitContainer = new HashMap<>();
		openViews = new ArrayList<>();
		availableViews = new ArrayList<>();
		listeners = new ArrayList<>();

		List<ExtensionHandle<IViewDescriptor>> viewDescriptors =
				context.getExtensions(IDeveloperBundle.EPT_VIEW, IViewDescriptor.class);
		for (ExtensionHandle<IViewDescriptor> view : viewDescriptors) {
			availableViews.add(view.get());
		}

		availableViews.sort(new ViewNameComparator());

		config = (PerspectiveManagerConfig) context.getConfigurationObject(CONFIG_PATH);
	}

	private boolean isInitialized()
	{
		return isViewSplitContainerCreated && isLeftViewBarContainerCreated;
	}

	@Override
	public ViewSplitContainer createViewSplitContainer()
	{
		ViewSplitContainer container;
		if (viewSplitContainer.isEmpty() && config != null) {
			List<ViewSplitContainerConfig> configs = config.getSplitContainerConfigs();
			if (configs.size() > containerCount) {
				container = new ViewSplitContainer(this, null, configs.get(containerCount));
			} else {
				container = new ViewSplitContainer(this);
			}
		} else {
			container = new ViewSplitContainer(this);
		}

		containerCount++;
		viewSplitContainer.put(container.getContainerID(), container);
		isViewSplitContainerCreated = true;
		return container;
	}

	@Override
	public ViewBarContainer createLeftViewBarContainer()
	{
		if (viewBarContainer == null) {
			viewBarContainer = new ViewBarContainer("LeftBarContainer", this);

			if (config != null) {
				List<String> openViews = config.getLeftBarViews();
				if (openViews != null) {
					for (String viewID : openViews) {
						openView(viewID, viewBarContainer);
					}
				}
				int leftBarActiveView = config.getLeftBarActiveView();
				if (leftBarActiveView < 0) {
					viewBarContainer.collapse();
				} else {
					viewBarContainer.setActiveView(leftBarActiveView);
				}
			}
		}

		isLeftViewBarContainerCreated = true;
		return viewBarContainer;
	}

	@Override
	public boolean removeViewSplitContainer(ViewSplitContainer container)
	{
		if (viewSplitContainer.containsKey(container.getContainerID())) {
			// TODO: container.closeAllViews();
			viewSplitContainer.remove(container.getContainerID());
			return true;
		}

		return false;
	}

	@Override
	public List<IView> getRegisteredViews()
	{
		return openViews;
	}

	@Override
	public List<IViewDescriptor> getAvailableViews()
	{
		List<IViewDescriptor> views = new ArrayList<>();
		for (IViewDescriptor descriptor : availableViews) {
			if (!descriptor.requiresObject() && descriptor.isManuallyOpenable()) {
				views.add(descriptor);
			}
		}
		return views;
	}

	@Override
	public List<IViewDescriptor> getAvailableViews(Object o)
	{
		List<IViewDescriptor> views = new ArrayList<>();
		for (IViewDescriptor descriptor : availableViews) {
			if (descriptor.acceptsObject(o)) {
				views.add(descriptor);
			}
		}
		return views;
	}

	@Override
	public IViewDescriptor getView(String viewID)
	{
		for (IViewDescriptor viewDescriptor : availableViews) {
			if (viewDescriptor.getViewID().equals(viewID)) {
				return viewDescriptor;
			}
		}
		return null;
	}

	@Override
	public void setActiveView(IView view)
	{
		if (activeView != view) {
			activeView = view;
			publishActiveViewChanged();
		}
	}

	@Override
	public IView getActiveView()
	{
		return activeView;
	}

	@Override
	public IView openView(IViewDescriptor viewDescriptor)
	{
		return openView(viewDescriptor, (Object) null);
	}

	@Override
	public IView openView(IViewDescriptor viewDescriptor, Object o)
	{
		return openView(viewDescriptor, null, o);
	}

	@Override
	public IView openView(String viewID, IViewContainer container)
	{
		return openView(viewID, container, null);
	}

	@Override
	public IView openView(String viewID, IViewContainer container, Object o)
	{
		IViewDescriptor view = null;

		for (IViewDescriptor p : availableViews) {
			if (p.getViewID().equals(viewID)) {
				view = p;
				break;
			}
		}

		if (view != null) {
			return openView(view, container, o);
		}

		return null;
	}

	@Override
	public IView openView(IViewDescriptor viewDescriptor, IViewContainer container, Object o)
	{
		if (viewDescriptor == null) {
			return null;
		}

		// If container is null, fetch the next best one
		if (container == null) {
			for (String key : viewSplitContainer.keySet()) {
				container = viewSplitContainer.get(key);
				break;
			}
		}

		// If there exists no container, do nothing
		if (container == null) {
			System.err.println("No container available to add a view!");
			return null;
		}

		// Are we allowed to open this view?
		if (o == null && viewDescriptor.requiresObject()) {
			return null;
		} else if (o != null && !viewDescriptor.acceptsObject(o)) {
			return null;
		}
		IView newView = viewDescriptor.createNewView();

		if (o != null) {
			newView.selectObject(o);
		}

		Pair<String, Integer> uniqueViewInfo = getUniqueViewName(newView);
		newView.setName(uniqueViewInfo.getFirst());
		newView.setIndex(uniqueViewInfo.getSecond());

		openViews.add(newView);
		container.addView(newView);
		activeView = newView;

		publishViewOpened(newView);
		publishActiveViewChanged();

		if (isInitialized()) {
			saveConfiguration();
		}

		return newView;
	}

	private Pair<String, Integer> getUniqueViewName(IView newView)
	{
		String name = newView.getName();
		int num = 0;

		while (!isNameUnique(name)) {
			num++;
			name = String.format("%s (%d)", newView.getName(), num);
		}

		return new Pair<>(name, num);
	}

	private boolean isNameUnique(String name)
	{
		return openViews.stream().noneMatch(v -> v.getName().equals(name));
	}

	/**
	 * Tries to find a view that accepts an object and opens the view with it if
	 * found.
	 */
	@Override
	public IView openView(Object o, IViewContainer container)
	{
		for (IViewDescriptor view : getAvailableViews(o)) {
			return openView(view, container, o);
		}
		return null;
	}

	@Override
	public IView openView(Object o)
	{
		return openView(o, null);
	}

	@Override
	public boolean closeView(IView view)
	{
		if (view != null && view.isClosable()) {
			view.onClose();
			openViews.remove(view);

			for (ViewSplitContainer container : viewSplitContainer.values()) {
				container.removeView(view);
			}

			if (view == activeView) {
				setActiveView(null);
			}

			saveConfiguration();

			return true;
		}
		return false;
	}

	private boolean isClosable()
	{
		for (IView view : getRegisteredViews()) {
			if (!view.isClosable()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onWindowClosing()
	{
		saveConfiguration();
		boolean closable = isClosable();
		if (closable) {
			for (IView view : getRegisteredViews()) {
				view.onClose();
			}
		}
		return closable;
	}

	private void saveConfiguration()
	{
		List<IView> views = viewBarContainer.getOpenViews();
		List<String> leftBarViews = new ArrayList<>();

		for (IView view : views) {
			leftBarViews.add(view.getDescriptor().getViewID());
		}

		int leftBarActiveView = viewBarContainer.getActiveView();

		List<ViewSplitContainerConfig> splitContainerConfigs = new ArrayList<>();
		for (ViewSplitContainer container : viewSplitContainer.values()) {
			splitContainerConfigs.add(container.getConfig());
		}

		config = new PerspectiveManagerConfig(leftBarViews, leftBarActiveView, splitContainerConfigs);

		context.saveConfigurationObject(CONFIG_PATH, config);
	}

	// --------------------------------------------------
	// Listener Methods
	// --------------------------------------------------
	@Override
	public void addPerspectiveManagerListener(IPerspectiveManagerListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removePerspectiveManagerListener(IPerspectiveManagerListener listener)
	{
		listeners.remove(listener);
	}

	private void publishActiveViewChanged()
	{
		for (IPerspectiveManagerListener l : listeners) {
			l.activeViewChanged();
		}
	}

	private void publishViewOpened(IView view)
	{
		for (IPerspectiveManagerListener l : listeners) {
			l.viewOpened(view);
		}
	}

	private void publishViewClosed(IView view)
	{
		for (IPerspectiveManagerListener l : listeners) {
			l.viewClosed(view);
		}
	}

	private class ViewNameComparator implements Comparator<IViewDescriptor>
	{
		@Override
		public int compare(IViewDescriptor o1, IViewDescriptor o2)
		{
			return Collator.getInstance().compare(o1.getTitle(), o2.getTitle());
		}
	}
}
