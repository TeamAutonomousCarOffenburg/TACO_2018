package hso.autonomy.tools.util.swing.command;

/**
 * @author Stefan Glaser
 */
public interface IExecutableCommand<T> extends ICommand {
	/**
	 * Execute this Command on the given user object
	 */
	boolean execute(T userObject);
}
