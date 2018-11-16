package hso.autonomy.tools.developer.bundles.developer.window.statusBar;

import javax.swing.Icon;
import javax.swing.JComponent;

public interface IStatusObserver {
	String getName();

	Icon getIcon();

	JComponent getDetailedStatusPanel();

	boolean addObserverListener(IStatusObserverListener listener);

	boolean removeObserverListener(IStatusObserverListener listener);
}
