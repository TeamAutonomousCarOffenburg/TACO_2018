package hso.autonomy.tools.developer.bundles.developer.window.statusBar;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;

import hso.autonomy.tools.util.bundleFramework.BundleContext;

public abstract class StatusObserverBase implements IStatusObserver
{
	protected final BundleContext context;

	protected String name;

	protected Icon icon;

	protected List<IStatusObserverListener> listeners;

	protected JComponent detailedStatusPanel;

	public StatusObserverBase(BundleContext context, String name, Icon icon)
	{
		this.context = context;
		this.name = name;
		this.icon = icon;
		this.listeners = new ArrayList<>();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}

	@Override
	public JComponent getDetailedStatusPanel()
	{
		if (detailedStatusPanel == null) {
			detailedStatusPanel = createDetailedStatusPanel();
		}
		return detailedStatusPanel;
	}

	protected abstract JComponent createDetailedStatusPanel();

	@Override
	public boolean addObserverListener(IStatusObserverListener listener)
	{
		return listeners.add(listener);
	}

	@Override
	public boolean removeObserverListener(IStatusObserverListener listener)
	{
		return listeners.remove(listener);
	}

	protected void publishMessage(String msg)
	{
		for (IStatusObserverListener listener : listeners) {
			listener.publishStatusMessage(msg);
		}
	}

	public void publishStateChanged()
	{
		for (IStatusObserverListener listener : listeners) {
			listener.observerStateChanged(this);
		}
	}
}
