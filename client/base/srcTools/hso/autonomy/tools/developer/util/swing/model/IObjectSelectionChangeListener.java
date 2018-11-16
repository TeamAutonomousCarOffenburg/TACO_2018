package hso.autonomy.tools.developer.util.swing.model;

public interface IObjectSelectionChangeListener<T> {
	void objectSelected(T newObject);

	void objectDeselected(T removedObject);
}
