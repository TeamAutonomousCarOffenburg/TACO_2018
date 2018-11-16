package hso.autonomy.tools.developer.bundles.developer.explorer.ui;

import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import hso.autonomy.tools.developer.bundles.developer.explorer.model.ITreeNode;

public class ExplorerTree extends JTree
{
	public ExplorerTree(TreeModel newModel)
	{
		super(newModel);
	}

	@Override
	public String getToolTipText(MouseEvent event)
	{
		TreePath curPath = getPathForLocation(event.getX(), event.getY());
		if (curPath == null) {
			return null;
		}

		ITreeNode node = (ITreeNode) curPath.getLastPathComponent();
		return node.getToolTipText();
	}
}
