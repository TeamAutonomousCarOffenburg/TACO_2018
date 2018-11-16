package hso.autonomy.tools.util.swing.command;

import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import hso.autonomy.tools.util.swing.model.IDisplayableItem;
import hso.autonomy.tools.util.swing.model.ChangeableSupport;

/**
 * @author Stefan Glaser
 */
public class CommandListWrapper extends ChangeableSupport implements ICommandList, ChangeListener
{
	private ICommandList commandList;

	public CommandListWrapper(ICommandList commandList)
	{
		setCommandList(commandList);
	}

	public void setCommandList(ICommandList newCommandList)
	{
		if (commandList != null) {
			commandList.removeChangeListener(this);
		}

		commandList = newCommandList;
		fireStateChanged();

		if (commandList != null) {
			commandList.addChangeListener(this);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		fireStateChanged();
	}

	@Override
	public IDisplayableItem getUndoCommand()
	{
		if (commandList != null) {
			return commandList.getUndoCommand();
		}

		return null;
	}

	@Override
	public IDisplayableItem getRedoCommand()
	{
		if (commandList != null) {
			return commandList.getRedoCommand();
		}

		return null;
	}

	@Override
	public boolean undo()
	{
		if (commandList != null) {
			return commandList.undo();
		}

		return false;
	}

	@Override
	public boolean redo()
	{
		if (commandList != null) {
			return commandList.redo();
		}

		return false;
	}

	@Override
	public List<? extends IDisplayableItem> getHistory()
	{
		if (commandList != null) {
			return commandList.getHistory();
		}

		return Collections.emptyList();
	}

	@Override
	public int getCurrentIndex()
	{
		if (commandList != null) {
			return commandList.getCurrentIndex();
		}

		return -1;
	}

	@Override
	public void setSaveIndex()
	{
		if (commandList != null) {
			commandList.setSaveIndex();
		}
	}

	@Override
	public boolean hasUnsavedChanges()
	{
		if (commandList != null) {
			return commandList.hasUnsavedChanges();
		}

		return false;
	}
}
