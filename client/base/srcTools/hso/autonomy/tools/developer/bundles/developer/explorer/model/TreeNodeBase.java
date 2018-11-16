package hso.autonomy.tools.developer.bundles.developer.explorer.model;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;

public abstract class TreeNodeBase<T, C extends ITreeNode> implements ITreeNode
{
	protected ITreeNode parent;

	protected final Vector<C> childNodes;

	protected Icon defaultIcon;

	protected String title;

	protected int fontStyle;

	protected T userObject;

	protected final List<Action> actionList;

	public TreeNodeBase(Icon defaultIcon, String title)
	{
		this(defaultIcon, title, null);
	}

	public TreeNodeBase(Icon defaultIcon, String title, T userObject)
	{
		this.defaultIcon = defaultIcon;
		this.title = title;
		this.fontStyle = Font.PLAIN;
		this.userObject = userObject;
		this.actionList = new ArrayList<>();

		childNodes = new Vector<>();
	}

	@Override
	public void nodeStructureChanged(ITreeNode node)
	{
		if (parent != null) {
			parent.nodeStructureChanged(node);
		}
	}

	@Override
	public void nodeContentChanged(ITreeNode node)
	{
		if (parent != null) {
			parent.nodeContentChanged(node);
		}
	}

	@Override
	public Icon getIcon()
	{
		return defaultIcon;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public int getFontStyle()
	{
		return fontStyle;
	}

	public void setUserObject(T userObject)
	{
		this.userObject = userObject;
	}

	@Override
	public T getUserObject()
	{
		return userObject;
	}

	@Override
	public List<Action> getUserActions()
	{
		return actionList;
	}

	@Override
	public void setParent(ITreeNode parent)
	{
		this.parent = parent;
	}

	public void addChild(C child)
	{
		childNodes.add(child);
		child.setParent(this);
		nodeStructureChanged(this);
	}

	public void insert(C child, int index)
	{
		if (index < childNodes.size()) {
			childNodes.insertElementAt(child, index);
		} else {
			childNodes.add(child);
		}
		child.setParent(this);
		nodeStructureChanged(this);
	}

	public void remove(C node)
	{
		childNodes.remove(node);
		nodeStructureChanged(this);
	}

	public void remove(int index)
	{
		if (index < childNodes.size()) {
			childNodes.remove(index);
			nodeStructureChanged(this);
		}
	}

	@Override
	public ITreeNode getParent()
	{
		return parent;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public ITreeNode getChildAt(int childIndex)
	{
		return childNodes.get(childIndex);
	}

	@Override
	public int getChildCount()
	{
		return childNodes.size();
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return childNodes.indexOf(node);
	}

	@Override
	public boolean isLeaf()
	{
		return childNodes.size() == 0;
	}

	@Override
	public Enumeration<ITreeNode> children()
	{
		return new Enumeration<ITreeNode>() {
			int count = 0;

			@Override
			public boolean hasMoreElements()
			{
				return count < childNodes.size();
			}

			@Override
			public ITreeNode nextElement()
			{
				synchronized (childNodes)
				{
					if (count < childNodes.size()) {
						return childNodes.get(count++);
					}
				}
				throw new NoSuchElementException("Vector Enumeration");
			}
		};
	}

	@Override
	public String getToolTipText()
	{
		return null;
	}
}
