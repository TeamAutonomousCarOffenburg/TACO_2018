package hso.autonomy.tools.developer.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import hso.autonomy.tools.developer.util.swing.model.SingleObjectSelectionModel;
import hso.autonomy.tools.util.swing.model.IDisplayableItem;

public class SingleItemSelectorToolBar<T extends IDisplayableItem> extends JToolBar implements ChangeListener
{
	private final List<T> itemList;

	private SingleObjectSelectionModel<T> selectionModel;

	private List<ItemToggleButton> toggleBtnList;

	public SingleItemSelectorToolBar(List<T> itemList, SingleObjectSelectionModel<T> selectionModel)
	{
		this.itemList = itemList;
		this.selectionModel = selectionModel;
		this.toggleBtnList = new ArrayList<ItemToggleButton>();

		initializeComponents();

		this.selectionModel.addListener(this);
	}

	private void initializeComponents()
	{
		setFloatable(false);

		ItemToggleButton toggleBtn;

		for (T item : itemList) {
			toggleBtn = new ItemToggleButton(item, selectionModel.getSelectedItem() == item);
			toggleBtnList.add(toggleBtn);
			add(toggleBtn);
		}
	}

	private void selectItem(T item)
	{
		if (selectionModel.getSelectedItem() == item) {
			selectionModel.setSelectedItem(null);
		} else {
			selectionModel.setSelectedItem(item);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		toggleBtnList.forEach(ItemToggleButton::refresh);
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
			selectItem(item);
		}

		public void refresh()
		{
			setSelected(item == selectionModel.getSelectedItem());
		}
	}
}
