package hso.autonomy.tools.developer.bundles.developer.perspective;

import hso.autonomy.tools.util.swing.model.IDisplayableItem;

public interface IViewDescriptor extends IDisplayableItem {
	String getViewID();

	IView createNewView();

	boolean acceptsObject(Object o);

	boolean requiresObject();

	boolean isManuallyOpenable();
}
