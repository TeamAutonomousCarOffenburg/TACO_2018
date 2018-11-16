package hso.autonomy.tools.util.bundleFramework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;
import hso.autonomy.tools.util.bundleFramework.service.ServiceHandle;

public class BundleManager implements IBundleManager
{
	private final Map<String, BundleContext> bundles;

	private final Workspace ws;

	public BundleManager()
	{
		this(null);
	}

	public BundleManager(String wsPath)
	{
		ws = new Workspace(wsPath);
		bundles = new HashMap<>();
	}

	/**
	 * Called initially once to load all bundles (register services,
	 * extensionPoints and extensions)
	 */
	@Override
	public void loadBundles(String bundlePath)
	{
		ws.loadWorkspace();

		// Check bundle consistency
		// checkBundleConsistency(bundles);

		List<Bundle> bundleList = BundleLoader.getBundles(bundlePath);
		BundleContext bundleContext;

		for (Bundle bundle : bundleList) {
			bundleContext = new BundleContext(bundle, this, ws);
			bundle.getBundleActivator().activateBundle(bundleContext);
			this.bundles.put(bundleContext.getDescriptor().getName(), bundleContext);
		}
	}

	@Override
	public <T> ServiceHandle<T> getService(String serviceID, Class<T> type)
	{
		if (serviceID != null && serviceID.contains(".")) {
			String bundleName = serviceID.substring(0, serviceID.indexOf("."));
			BundleContext bundle = bundles.get(bundleName);

			if (bundle != null) {
				return bundle.getServiceHandleFor(serviceID, type);
			} else {
				System.err.println("bundle: " + bundleName + " not found!");
			}
		}

		return null;
	}

	@Override
	public <T> List<ExtensionHandle<T>> getExtensions(String extensionPointID, Class<T> type)
	{
		if (extensionPointID == null) {
			return null;
		}

		List<ExtensionHandle<T>> extensionHandles = new ArrayList<>();

		for (BundleContext bundle : bundles.values()) {
			extensionHandles.addAll(bundle.getExtensionHandlesFor(extensionPointID, type));
		}

		return extensionHandles;
	}

	@Override
	public void shutdown()
	{
		// Save Workspace
		ws.saveWorkspace();

		// Deactivate all bundles
		for (BundleContext bundle : bundles.values()) {
			bundle.bundle.getBundleActivator().deactivateBundle();
		}

		System.exit(0);
	}
}
