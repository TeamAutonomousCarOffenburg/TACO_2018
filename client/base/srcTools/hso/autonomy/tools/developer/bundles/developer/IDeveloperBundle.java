package hso.autonomy.tools.developer.bundles.developer;

public interface IDeveloperBundle {
	// General Constants
	String BUNDLE_NAME = "developer";

	String DOT = ".";

	// Services
	String SRV_WINDOW_MANAGER = BUNDLE_NAME + DOT + "windowManager";

	String SRV_PERSPECTIVE_MANAGER = BUNDLE_NAME + DOT + "perspectiveManager";

	String SRV_PROCESSOR = BUNDLE_NAME + DOT + "processor";

	String SRV_EXPLORER = BUNDLE_NAME + DOT + "explorer";

	// ExtensionPoints
	String EPT_VIEW = SRV_PERSPECTIVE_MANAGER + DOT + "view";

	String EPT_MENU = SRV_WINDOW_MANAGER + DOT + "menu";

	String EPT_NEW_WIZARD = SRV_WINDOW_MANAGER + DOT + "newWizard";

	String EPT_IMPORT_WIZARD = SRV_WINDOW_MANAGER + DOT + "importWizard";

	String EPT_EXPORT_WIZARD = SRV_WINDOW_MANAGER + DOT + "exportWizard";

	String EPT_RUN_WIZARD = SRV_WINDOW_MANAGER + DOT + "runWizard";

	String EPT_OPEN_ACTION = SRV_WINDOW_MANAGER + DOT + "openAction";

	String EPT_EXPLORER_ENTITY = SRV_EXPLORER + DOT + "entity";

	String EPT_STATUS_OBSERVER = SRV_WINDOW_MANAGER + DOT + "statusObserver";

	// Extensions
	String EX_EXPLORER_VIEW = EPT_VIEW + DOT + "explorerView";

	String EX_PROCESSOR_VIEW = EPT_VIEW + DOT + "processorView";

	String EX_PROCESSOR_STATUS_OBSERVER = EPT_STATUS_OBSERVER + DOT + "processorStatusObserver";
}
