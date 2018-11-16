package hso.autonomy.tools.util.swing.command;

import java.util.List;

import hso.autonomy.tools.util.swing.model.IChangeable;
import hso.autonomy.tools.util.swing.model.IDisplayableItem;

/**
 * @author Stefan Glaser
 */
public interface ICommandList extends IChangeable {
	/**
	 * Retrieve the current Undo-Command.
	 *
	 * @return the current Undo-Command
	 */
	IDisplayableItem getUndoCommand();

	/**
	 * Retrieve the current Redo-Command.
	 *
	 * @return the current Redo-Command
	 */
	IDisplayableItem getRedoCommand();

	/**
	 * Undo current Undo-Command.
	 *
	 * @return success
	 */
	boolean undo();

	/**
	 * Redo current Redo-Command.
	 *
	 * @return success
	 */
	boolean redo();

	/**
	 * Retrieve the list of commands.
	 *
	 * @return the list of commands
	 */
	List<? extends IDisplayableItem> getHistory();

	/**
	 * Retrieve the index of the current Command in the history.
	 *
	 * @return the index of the current Command in the Command history
	 */
	int getCurrentIndex();

	/**
	 * Indicates that the current index should be remembered as save index.
	 */
	void setSaveIndex();

	/**
	 * Whether the save index still matches the current one.
	 */
	boolean hasUnsavedChanges();
}
