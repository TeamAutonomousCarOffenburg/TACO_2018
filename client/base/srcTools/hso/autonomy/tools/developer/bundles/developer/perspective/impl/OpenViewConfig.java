package hso.autonomy.tools.developer.bundles.developer.perspective.impl;

public class OpenViewConfig
{
	private String ID;

	private Object selectedObject;

	public OpenViewConfig(String ID, Object selectedObject)
	{
		this.ID = ID;
		this.selectedObject = selectedObject;
	}

	public String getID()
	{
		return ID;
	}

	public Object getSelectedObject()
	{
		return selectedObject;
	}
}
