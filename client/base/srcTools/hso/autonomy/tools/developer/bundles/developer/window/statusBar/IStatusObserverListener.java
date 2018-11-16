package hso.autonomy.tools.developer.bundles.developer.window.statusBar;

public interface IStatusObserverListener {
	void publishStatusMessage(String statusMsg);

	void observerStateChanged(IStatusObserver statusObserver);
}
