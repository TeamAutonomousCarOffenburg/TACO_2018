package hso.autonomy.tools.util.swing.command;

import hso.autonomy.tools.util.swing.model.IDisplayableItem;

/**
 * Basic Interface for Commands performing operations on models.<br>
 * The execution of a Command can be reversed or repeated, if the Command allows
 * such operations.<br>
 * <b>Note:</b>An instance of {@link ICommand} doesn't provide a method for the
 * initial execution of the Command. It only provides Undo and Redo
 * accessibility options!
 *
 * @author Stefan Glaser
 */
public interface ICommand extends IDisplayableItem {
	/**
	 * Merge new command into this one.<br>
	 * If merge was successful, the new command is expected to be part of this
	 * command and will thus be not added as new command to the command list.<br>
	 * <br>
	 * Example:<br>
	 * If the same command instance is performed multiple times in a row, e.g. to
	 * provide user feedback during a drag operation, the command can "merge"
	 * itself in order tell the command list not to be added multiple times as a
	 * new command to the list.
	 *
	 * @return true if new command was successfully merged into this command,
	 *         false otherwise
	 */
	boolean merge(ICommand newCommand);

	/**
	 * @return true if this command can be undone and redone
	 */
	boolean isRepeatable();

	/**
	 * Undo this Command.
	 *
	 * @return success
	 */
	boolean undo();

	/**
	 * Redo this Command.
	 *
	 * @return success
	 */
	boolean redo();
}
