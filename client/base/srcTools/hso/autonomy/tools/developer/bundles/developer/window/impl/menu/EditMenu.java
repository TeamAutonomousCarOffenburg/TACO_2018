package hso.autonomy.tools.developer.bundles.developer.window.impl.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManagerListener;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.imageBuffer.ImageFile;
import hso.autonomy.tools.util.swing.command.ICommandList;
import hso.autonomy.tools.util.swing.model.IDisplayableItem;

/**
 * The File menu.
 *
 * @author Stefan Glaser
 */
public class EditMenu extends JMenu implements IPerspectiveManagerListener, ChangeListener
{
	private final IPerspectiveManager perspectiveManager;

	private ICommandList commandList;

	private final UndoAction undoAction;

	private final RedoAction redoAction;

	public EditMenu(BundleContext context)
	{
		super("Edit");
		setMnemonic(KeyEvent.VK_E);

		perspectiveManager = IDeveloperService.PERSPECTIVE_MANAGER.get(context);

		undoAction = new UndoAction(null);
		redoAction = new RedoAction(null);

		add(new JMenuItem(undoAction));
		add(new JMenuItem(redoAction));

		activeViewChanged();
		perspectiveManager.addPerspectiveManagerListener(this);
	}

	@Override
	public void activeViewChanged()
	{
		if (commandList != null) {
			commandList.removeChangeListener(this);
		}

		IView activeView = perspectiveManager.getActiveView();
		if (activeView != null) {
			commandList = activeView.getCommandList();
		} else {
			commandList = null;
		}
		updateCommandActions();

		if (commandList != null) {
			commandList.addChangeListener(this);
		}
	}

	@Override
	public void viewClosed(IView view)
	{
	}

	@Override
	public void viewOpened(IView newView)
	{
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		// CommandList has changed, so refresh CommandList menu items
		updateCommandActions();
	}

	private void updateCommandActions()
	{
		IDisplayableItem undoCommand = null;
		IDisplayableItem redoCommand = null;

		if (commandList != null) {
			undoCommand = commandList.getUndoCommand();
			redoCommand = commandList.getRedoCommand();
		}

		undoAction.setCommand(undoCommand);
		redoAction.setCommand(redoCommand);
	}

	private class UndoAction extends AbstractAction
	{
		private IDisplayableItem command;

		public UndoAction(IDisplayableItem undoCommand)
		{
			super("Undo", ImageFile.UNDO.getIcon());
			putValue(SHORT_DESCRIPTION, "Undo previous operation");
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));

			setCommand(undoCommand);
		}

		public void setCommand(IDisplayableItem undoCommand)
		{
			command = undoCommand;

			if (command != null) {
				putValue(Action.NAME, "Undo: " + command.getTitle());
				putValue(SHORT_DESCRIPTION, "Undo: " + command.getToolTip());
				setEnabled(true);
			} else {
				putValue(Action.NAME, "Undo");
				putValue(SHORT_DESCRIPTION, "Undo previous operation");
				setEnabled(false);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			commandList.undo();
		}
	}

	private class RedoAction extends AbstractAction
	{
		private IDisplayableItem command;

		public RedoAction(IDisplayableItem redoCommand)
		{
			super("Redo", ImageFile.REDO.getIcon());
			putValue(SHORT_DESCRIPTION, "Redo previously undone operation");
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));

			setCommand(redoCommand);
		}

		public void setCommand(IDisplayableItem redoCommand)
		{
			command = redoCommand;

			if (command != null) {
				putValue(Action.NAME, "Redo: " + command.getTitle());
				putValue(SHORT_DESCRIPTION, "Redo: " + command.getToolTip());
				setEnabled(true);
			} else {
				putValue(Action.NAME, "Redo");
				putValue(SHORT_DESCRIPTION, "Redo previously undone operation");
				setEnabled(false);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			commandList.redo();
		}
	}
}