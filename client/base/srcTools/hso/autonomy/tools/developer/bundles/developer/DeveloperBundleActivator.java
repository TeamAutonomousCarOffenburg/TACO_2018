package hso.autonomy.tools.developer.bundles.developer;

import hso.autonomy.tools.developer.bundles.developer.explorer.model.impl.Explorer;
import hso.autonomy.tools.developer.bundles.developer.explorer.ui.ExplorerViewDescriptor;
import hso.autonomy.tools.developer.bundles.developer.perspective.impl.PerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.processor.model.impl.Processor;
import hso.autonomy.tools.developer.bundles.developer.processor.ui.ProcessorStatusObserver;
import hso.autonomy.tools.developer.bundles.developer.processor.ui.ProcessorViewDescriptor;
import hso.autonomy.tools.developer.bundles.developer.window.impl.WindowManager;
import hso.autonomy.tools.developer.util.properties.impl.PropertiesWrapper;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.bundleFramework.BundleFactoryBase;
import hso.autonomy.tools.util.bundleFramework.IBundleActivator;

public class DeveloperBundleActivator implements IBundleActivator, IDeveloperBundle
{
	private BundleContext context;

	private WindowManager windowManager;

	@Override
	public void activateBundle(BundleContext bundleContext)
	{
		context = bundleContext;

		bundleContext.setBundleFactory(new DeveloperBundleFactory(bundleContext));
		DeveloperProperties.propertiesWrapper = new PropertiesWrapper(bundleContext.getBundleProperties());
	}

	@Override
	public void deactivateBundle()
	{
		if (windowManager != null) {
			windowManager.disposeWindows();
		}

		if (context != null) {
			context.saveBundleProperties();
			context = null;
		}
	}

	class DeveloperBundleFactory extends BundleFactoryBase
	{
		public DeveloperBundleFactory(BundleContext bundleContext)
		{
			super(bundleContext);
		}

		@Override
		public Object createExtensionInstanceFor(String extensionID)
		{
			switch (extensionID) {
			case EX_EXPLORER_VIEW:
				return new ExplorerViewDescriptor(bundleContext);
			case EX_PROCESSOR_VIEW:
				return new ProcessorViewDescriptor(bundleContext);
			case EX_PROCESSOR_STATUS_OBSERVER:
				return new ProcessorStatusObserver(bundleContext);
			}

			return null;
		}

		@Override
		public Object createServiceInstanceFor(String serviceID)
		{
			switch (serviceID) {
			case SRV_WINDOW_MANAGER:
				windowManager = new WindowManager(bundleContext);
				return windowManager;
			case SRV_PERSPECTIVE_MANAGER:
				return new PerspectiveManager(bundleContext);
			case SRV_EXPLORER:
				return new Explorer(bundleContext);
			case SRV_PROCESSOR:
				return new Processor();
			}

			return null;
		}
	}
}
