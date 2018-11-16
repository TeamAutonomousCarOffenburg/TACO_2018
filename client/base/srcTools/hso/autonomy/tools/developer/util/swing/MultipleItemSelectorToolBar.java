package hso.autonomy.tools.developer.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import hso.autonomy.tools.developer.util.swing.model.IObjectSelectionChangeListener;
import hso.autonomy.tools.developer.util.swing.model.MultipleObjectSelectionModel;
import hso.autonomy.tools.util.swing.model.IDisplayableItem;

public class MultipleItemSelectorToolBar<T extends IDisplayableItem>
		extends JToolBar implements IObjectSelectionChangeListener<T>
{
	private final List<T> itemList;

	private MultipleObjectSelectionModel<T> selectionModel;

	private List<ItemToggleButton> toggleBtnList;

	public MultipleItemSelectorToolBar(List<T> itemList, MultipleObjectSelectionModel<T> selectionModel)
	{
		this.itemList = itemList;
		this.selectionModel = selectionModel;
		this.toggleBtnList = new ArrayList<>();

		initializeComponents();

		this.selectionModel.addListener(this);
	}

	private void initializeComponents()
	{
		setBorder(null);
		setBorderPainted(false);
		setFloatable(false);

		ItemToggleButton toggleBtn;

		for (T item : itemList) {
			toggleBtn = new ItemToggleButton(item, selectionModel.isItemSelected(item));
			toggleBtnList.add(toggleBtn);
			add(toggleBtn);
		}
	}

	@Override
	public void objectSelected(T newObject)
	{
		int index = itemList.indexOf(newObject);

		if (index == -1) {
			toggleBtnList.get(index).setEnabled(true);
		}
	}

	@Override
	public void objectDeselected(T removedObject)
	{
		int index = itemList.indexOf(removedObject);

		if (index == -1) {
			toggleBtnList.get(index).setEnabled(false);
		}
	}

	// --------------------------------------------------
	// Actions
	// --------------------------------------------------
	private class ItemToggleButton extends JToggleButton implements ActionListener
	{
		final T item;

		public ItemToggleButton(T item, boolean selected)
		{
			super(item.getIcon(), selected);
			this.item = item;
			if (item.getIcon() == null) {
				setText(item.getTitle());
			}
			setToolTipText(item.getTitle());
			this.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (isSelected()) {
				selectionModel.addSelectedItem(item);
			} else {
				selectionModel.removeSelectedItem(item);
			}
		}
	}
}
