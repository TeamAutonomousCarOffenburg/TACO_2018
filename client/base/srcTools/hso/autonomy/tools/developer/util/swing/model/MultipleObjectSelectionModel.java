package hso.autonomy.tools.developer.util.swing.model;

import java.util.ArrayList;
import java.util.List;

public class MultipleObjectSelectionModel<T>
{
	private ArrayList<T> selectedItems;

	private List<IObjectSelectionChangeListener<T>> listeners;

	public MultipleObjectSelectionModel()
	{
		listeners = new ArrayList<>();
		selectedItems = new ArrayList<>();
	}

	public void addSelectedItem(T itemToAdd)
	{
		if (itemToAdd != null && !selectedItems.contains(itemToAdd)) {
			selectedItems.add(itemToAdd);
			publishObjectSelected(itemToAdd);
		}
	}

	public void removeSelectedItem(T itemToRemove)
	{
		if (itemToRemove != null && selectedItems.contains(itemToRemove)) {
			selectedItems.remove(itemToRemove);
			publishObjectDeselected(itemToRemove);
		}
	}

	public boolean isItemSelected(T item)
	{
		return selectedItems.contains(item);
	}

	public ArrayList<T> getSelectedItems()
	{
		return selectedItems;
	}

	public boolean isSelected(T item)
	{
		return selectedItems.contains(item);
	}

	protected void publishObjectSelected(T newObject)
	{
		for (IObjectSelectionChangeListener<T> l : listeners) {
			l.objectSelected(newObject);
		}
	}

	protected void publishObjectDeselected(T oldObject)
	{
		for (IObjectSelectionChangeListener<T> l : listeners) {
			l.objectDeselected(oldObject);
		}
	}

	public boolean addListener(IObjectSelectionChangeListener<T> l)
	{
		return listeners.add(l);
	}

	public boolean removeListener(IObjectSelectionChangeListener<T> l)
	{
		return listeners.remove(l);
	}

	public void clearSelection()
	{
		while (selectedItems.size() > 0) {
			removeSelectedItem(selectedItems.get(0));
		}
	}
}
