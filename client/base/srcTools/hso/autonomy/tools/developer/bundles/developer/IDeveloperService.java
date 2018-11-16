package hso.autonomy.tools.developer.bundles.developer;

import hso.autonomy.tools.developer.bundles.developer.explorer.model.IExplorer;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.processor.model.IProcessor;
import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.util.bundleFramework.service.ServiceReference;

public interface IDeveloperService {
	ServiceReference<IWindowManager> WINDOW_MANAGER =
			new ServiceReference<>(IDeveloperBundle.SRV_WINDOW_MANAGER, IWindowManager.class);

	ServiceReference<IPerspectiveManager> PERSPECTIVE_MANAGER =
			new ServiceReference<>(IDeveloperBundle.SRV_PERSPECTIVE_MANAGER, IPerspectiveManager.class);

	ServiceReference<IProcessor> PROCESSOR = new ServiceReference<>(IDeveloperBundle.SRV_PROCESSOR, IProcessor.class);

	ServiceReference<IExplorer> EXPLORER = new ServiceReference<>(IDeveloperBundle.SRV_EXPLORER, IExplorer.class);
}
