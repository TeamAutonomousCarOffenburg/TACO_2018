package hso.autonomy.tools.developer.util.properties;

public interface IPropertiesWrapper {
	/**
	 * Retrieve the string value of the property assigned to the given key.
	 *
	 * @param key the key of the property
	 * @return the string value of the property
	 */
	String getProperty(String key);

	/**
	 * Retrieve the string value of the property assigned to the given key.
	 *
	 * @param key the key of the property
	 * @param defaultValue the default value if there is no valid value assigned
	 *        to the given key
	 * @return the string value of the property
	 */
	String getProperty(String key, String defaultValue);

	int getProperty(String key, int defaultValue);

	double getProperty(String key, double defaultValue);

	boolean getProperty(String key, boolean defaultValue);

	<T extends Enum<T>> T getProperty(String key, Class<T> enumType, T defaultValue);

	/**
	 * Retrieve the value of the property assigned to the given key.
	 *
	 * @param key the key of the property
	 * @return the value of the property
	 */
	Object get(String key);

	/**
	 * Set a property.
	 *
	 * @param key the key
	 * @param value the value
	 */
	void setProperty(String key, String value);

	void setProperty(String key, int value);

	void setProperty(String key, double value);

	void setProperty(String key, boolean value);
}