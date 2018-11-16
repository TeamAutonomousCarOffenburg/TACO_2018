package hso.autonomy.tools.util.swing.command;

import javax.swing.Icon;

/**
 * @author Stefan Glaser
 */
public abstract class ExecutableCommandBase<T> implements IExecutableCommand<T>
{
	/** The user object */
	protected T userObject;

	/** The title of this Command */
	protected String title;

	/** The description of this Command */
	protected String description;

	/** The tool-tip of this Command */
	protected String toolTip;

	/** The icon of this Command */
	protected Icon icon;

	/** Indicator if undo/redo actions are possible */
	protected boolean isRepeatable;

	public ExecutableCommandBase(String title, String description, String toolTip, Icon icon, boolean isRepeatable)
	{
		this.title = title;
		this.toolTip = toolTip;
		this.description = description;
		this.icon = icon;
		this.isRepeatable = isRepeatable;
	}

	@Override
	public boolean merge(ICommand newCommand)
	{
		return false;
	}

	@Override
	public boolean execute(T userObject)
	{
		this.userObject = userObject;

		if (userObject != null) {
			setUp();

			return doOperation();
		}

		return false;
	}

	@Override
	public boolean redo()
	{
		if (isRepeatable) {
			return doOperation();
		}

		return false;
	}

	@Override
	public boolean undo()
	{
		if (isRepeatable) {
			return undoOperation();
		}

		return false;
	}

	/**
	 * Update method called once before execution to setup Command operation
	 * values.<br>
	 * Default implementation does nothing.
	 */
	protected void setUp()
	{
	}

	/**
	 * The actual method performing the <b>initial command operation</b> and
	 * <b>redo operation</b>.
	 *
	 * @return success
	 */
	protected abstract boolean doOperation();

	/**
	 * The actual method performing the <b>undo</b> command operation.
	 *
	 * @return success
	 */
	protected abstract boolean undoOperation();

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public String getToolTip()
	{
		return toolTip;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}

	@Override
	public boolean isRepeatable()
	{
		return isRepeatable;
	}
}
