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
package hso.autonomy.tools.developer.bundles.developer.explorer.model;

import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;

public interface ITreeNode extends TreeNode {
	Object getUserObject();

	List<Action> getUserActions();

	void setParent(ITreeNode parent);

	void nodeStructureChanged(ITreeNode node);

	void nodeContentChanged(ITreeNode node);

	@Override
	ITreeNode getChildAt(int childIndex);

	@Override
	ITreeNode getParent();

	Icon getIcon();

	String getTitle();

	int getFontStyle();

	String getToolTipText();
}
