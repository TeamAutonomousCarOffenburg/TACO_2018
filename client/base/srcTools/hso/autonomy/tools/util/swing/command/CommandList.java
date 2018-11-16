package hso.autonomy.tools.util.swing.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hso.autonomy.tools.util.swing.model.IDisplayableItem;
import hso.autonomy.tools.util.swing.model.ChangeableSupport;

/**
 * @author Stefan Glaser
 */
public class CommandList extends ChangeableSupport implements ICommandList
{
	/** the list holding the undo commands */
	protected final ArrayList<ICommand> commands;

	/** index of the current active command */
	private int currentIndex;

	/** the maximal number of entries that should be stored */
	private final int maxCommands;

	/** Index of undo Command if available */
	private int undoIndex;

	/** Index of redo Command if available */
	private int redoIndex;

	private int saveIndex;

	public CommandList(int maxCommands)
	{
		this.maxCommands = maxCommands > 10 ? maxCommands : 10;

		commands = new ArrayList<>();
		currentIndex = -1;
		undoIndex = -1;
		redoIndex = -1;
		saveIndex = -1;
	}

	private void setCurrentIndex(int newIndex)
	{
		if (currentIndex == newIndex) {
			return;
		}

		currentIndex = newIndex;

		// Search for next undo option
		if (currentIndex >= 0) {
			if (commands.get(currentIndex).isRepeatable()) {
				undoIndex = currentIndex;
			} else {
				undoIndex = indexOfRepeatableCommandBeforeOrAt(currentIndex);
			}
		} else {
			undoIndex = -1;
		}

		// Search for next redo option
		redoIndex = indexOfRepeatableCommandAfter(currentIndex);

		fireStateChanged();
	}

	/**
	 * Add a newly executed Command to this command list.
	 *
	 * @param newCommand - the newly executed command
	 */
	public void addCommand(ICommand newCommand)
	{
		// make sure that all commands after the undo index are removed
		for (int i = commands.size() - 1; i > currentIndex; i--) {
			commands.remove(i);
		}

		// Try merge command into current command
		if (currentIndex >= 0 && commands.get(currentIndex).merge(newCommand)) {
			return;
		}

		commands.add(newCommand);
		setCurrentIndex(currentIndex + 1);

		// make sure that list does not grow too big
		if (commands.size() > maxCommands) {
			commands.remove(0);
			currentIndex--;
			undoIndex--;
			redoIndex--;
		}
	}

	@Override
	public ICommand getUndoCommand()
	{
		if (undoIndex >= 0) {
			return commands.get(undoIndex);
		}

		return null;
	}

	@Override
	public ICommand getRedoCommand()
	{
		if (redoIndex >= 0) {
			return commands.get(redoIndex);
		}

		return null;
	}

	@Override
	public boolean undo()
	{
		boolean success = false;

		if (undoIndex >= 0) {
			success = commands.get(undoIndex).undo();
			setCurrentIndex(undoIndex - 1);
		}

		return success;
	}

	@Override
	public boolean redo()
	{
		boolean success = false;

		if (redoIndex >= 0) {
			success = commands.get(redoIndex).redo();
			setCurrentIndex(redoIndex);
		}

		return success;
	}

	private int indexOfRepeatableCommandBeforeOrAt(int index)
	{
		int idx = index;
		while (idx >= 0 && !commands.get(idx).isRepeatable()) {
			idx--;
		}

		return idx;
	}

	private int indexOfRepeatableCommandAfter(int index)
	{
		int idx = index + 1;
		while (idx < commands.size() && !commands.get(idx).isRepeatable()) {
			idx++;
		}

		if (idx >= commands.size()) {
			return -1;
		}

		return idx;
	}

	@Override
	public List<? extends IDisplayableItem> getHistory()
	{
		return Collections.unmodifiableList(commands);
	}

	@Override
	public int getCurrentIndex()
	{
		return currentIndex;
	}

	public int size()
	{
		return commands.size();
	}

	@Override
	public void setSaveIndex()
	{
		saveIndex = getCurrentIndex();
	}

	@Override
	public boolean hasUnsavedChanges()
	{
		return saveIndex != getCurrentIndex();
	}
}