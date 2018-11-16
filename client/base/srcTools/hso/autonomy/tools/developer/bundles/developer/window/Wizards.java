package hso.autonomy.tools.developer.bundles.developer.window;

import hso.autonomy.tools.util.imageBuffer.ImageFile;

import java.awt.Image;

import javax.swing.Icon;

/**
 * A selection of default wizards provided by the window manager.
 *
 * @author Stefan Glaser
 */
public enum Wizards {
	/** The wizard for creating new things. */
	NEW_WIZARD("New", ImageFile.ADD.getImage(), "New...", null, "Select a wizard to create something new.", true),

	/** The wizard for importing existing stuff into the workspace. */
	IMPORT_WIZARD("Import", ImageFile.DOWN.getImage(), "Import...", null, "Select an import source.", true),

	/** The wizard for exporting stuff from the workspace. */
	EXPORT_WIZARD("Export", ImageFile.UP.getImage(), "Export...", null, "Select an export wizard.", true),

	/** The wizard for running things. */
	RUN_WIZARD("Run", ImageFile.EXECUTE.getImage(), "Run...", null, "Select a run target.", true);

	public final String dialogTitle;

	public final Image dialogImage;

	public final String title;

	public final Icon icon;

	public final String description;

	public final boolean isModal;

	private Wizards(String dialogTitle, Image dialogImage, String title, Icon icon, String description, boolean modal)
	{
		this.dialogTitle = dialogTitle;
		this.dialogImage = dialogImage;
		this.title = title;
		this.icon = icon;
		this.description = description;
		this.isModal = modal;
	}
}
