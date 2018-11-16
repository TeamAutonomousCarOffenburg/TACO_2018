package hso.autonomy.tools.developer.util;

public interface IDeveloperProperties {
	// --------------------------------------------------------------------
	// Explorer properties -------------------------------------------
	// --------------------------------------------------------------------
	/** @deprecated Just used internally as prefix. NOT FOR EXTERNAL USE!! */
	@Deprecated
	String EXPLORER = "explorer.";

	// --------------------------------------------------------------------
	// Perspective Manager properties ----------------------------------------
	// --------------------------------------------------------------------
	/** @deprecated Just used internally as prefix. NOT FOR EXTERNAL USE!! */
	@Deprecated
	String PERSPECTIVE = "perspective.";

	// --------------------------------------------------------------------
	// Processor properties ----------------------------------------
	// --------------------------------------------------------------------
	/** @deprecated Just used internally as prefix. NOT FOR EXTERNAL USE!! */
	@Deprecated
	String PROCESSOR = "processor.";

	// --------------------------------------------------------------------
	// Window Manager properties ----------------------------------------
	// --------------------------------------------------------------------
	/** @deprecated Just used internally as prefix. NOT FOR EXTERNAL USE!! */
	@Deprecated
	String WINDOW = "window.";

	// Wizards properties
	String PREFERRED_NEW_WIZARDS = WINDOW + "wizards.preferredNewWizards";

	String PREFERRED_IMPORT_WIZARDS = WINDOW + "wizards.preferredImportWizards";

	String PREFERRED_EXPORT_WIZARDS = WINDOW + "wizards.preferredExportWizards";

	String PREFERRED_RUN_WIZARDS = WINDOW + "wizards.preferredRunWizards";

	// Main Window Properties
	String MAIN_WINDOW_X = WINDOW + "mainWindow.x";

	String MAIN_WINDOW_Y = WINDOW + "mainWindow.y";

	String MAIN_WINDOW_WIDTH = WINDOW + "mainWindow.width";

	String MAIN_WINDOW_HEIGHT = WINDOW + "mainWindow.height";

	String MAIN_WINDOW_MAXIMIZED = WINDOW + "mainWindow.maximized";
}
