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
package hso.autonomy.tools.developer.bundles.developer.explorer.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.developer.bundles.developer.explorer.model.IExplorer;
import hso.autonomy.tools.developer.bundles.developer.explorer.model.IExplorerListener;
import hso.autonomy.tools.developer.bundles.developer.explorer.model.ITreeNode;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;

public class Explorer implements IExplorer
{
	private final List<IExplorerListener> listeners;

	private final List<ITreeNode> activeEntities;

	private final List<ExtensionHandle<ITreeNode>> availableEntities;

	private final Map<Class<?>, List<Action>> actionMap;

	public Explorer(BundleContext context)
	{
		listeners = new ArrayList<>();
		activeEntities = new ArrayList<>();
		actionMap = new HashMap<>();

		availableEntities = context.getExtensions(IDeveloperBundle.EPT_EXPLORER_ENTITY, ITreeNode.class);
		for (ExtensionHandle<ITreeNode> treeNode : availableEntities) {
			activateEntity(treeNode.get());
		}
	}

	@Override
	public List<ITreeNode> getActiveEntities()
	{
		return activeEntities;
	}

	@Override
	public List<ExtensionHandle<ITreeNode>> getAvailableEntities()
	{
		return availableEntities;
	}

	@Override
	public void activateEntity(ITreeNode node)
	{
		if (!activeEntities.contains(node)) {
			activeEntities.add(node);
			publishEntityActivated(node);
		}
	}

	@Override
	public void deactivateEntity(ITreeNode node)
	{
		if (activeEntities.remove(node)) {
			publishEntityDeactivated(node);
		}
	}

	@Override
	public boolean addListener(IExplorerListener listener)
	{
		return listeners.add(listener);
	}

	@Override
	public void registerAction(Action action, Class<?> actionClass)
	{
		List<Action> actionList = actionMap.get(actionClass);
		if (actionList == null) {
			// Create new action list
			actionList = new ArrayList<>();
			actionList.add(action);
			actionMap.put(actionClass, actionList);
		} else {
			// Just add new action
			actionList.add(action);
		}
	}

	@Override
	public List<Action> getActionList(Class<?> actionClass)
	{
		return actionMap.get(actionClass);
	}

	@Override
	public boolean removeListener(IExplorerListener listener)
	{
		return listeners.remove(listener);
	}

	private void publishEntityActivated(ITreeNode node)
	{
		for (IExplorerListener l : listeners) {
			l.entityActivated(node);
		}
	}

	private void publishEntityDeactivated(ITreeNode node)
	{
		for (IExplorerListener l : listeners) {
			l.entityDeactivated(node);
		}
	}
}
