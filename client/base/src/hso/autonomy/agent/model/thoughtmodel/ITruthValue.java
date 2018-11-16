package hso.autonomy.agent.model.thoughtmodel;

/**
 * Basic interface for all classes representing a non trivial truth value that requires some kind of memory.
 * @author kdorer
 */
public interface ITruthValue {
	/**
	 * Called each update cycle
	 * @param thoughtModel the thoughtmodel that can be used to determine the truth value
	 */
	void update(IThoughtModel thoughtModel);

	/**
	 * @return the truth value of this object
	 */
	boolean isValid();
}