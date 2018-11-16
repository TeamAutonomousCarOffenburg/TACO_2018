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
package hso.autonomy.tools.developer.bundles.developer.explorer.ui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import hso.autonomy.tools.developer.bundles.developer.explorer.model.DefaultTreeNode;
import hso.autonomy.tools.developer.bundles.developer.explorer.model.ITreeNode;

public class ExplorerTreeModel extends DefaultTreeModel
{
	public ExplorerTreeModel()
	{
		super(null);
		root = new RootNode();
	}

	@Override
	public void setRoot(TreeNode root)
	{
	}

	@Override
	public DefaultTreeNode getRoot()
	{
		return (DefaultTreeNode) this.root;
	}

	protected class RootNode extends DefaultTreeNode
	{
		public RootNode()
		{
			super("RootNode");
		}

		@Override
		public void nodeContentChanged(ITreeNode node)
		{
			ExplorerTreeModel.this.nodeChanged(node);
		}

		@Override
		public void nodeStructureChanged(ITreeNode node)
		{
			ExplorerTreeModel.this.reload(node);
		}
	}
}
