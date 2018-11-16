package hso.autonomy.tools.developer.util.properties.impl;

import java.util.Properties;

import hso.autonomy.tools.developer.util.properties.IPropertiesWrapper;

public class PropertiesWrapper implements IPropertiesWrapper
{
	private Properties properties;

	public PropertiesWrapper(Properties properties)
	{
		this.properties = properties;
	}

	@Override
	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		return properties.getProperty(key, defaultValue);
	}

	@Override
	public int getProperty(String key, int defaultValue)
	{
		String property = properties.getProperty(key);
		if (property == null) {
			return defaultValue;
		}

		int result;
		try {
			result = Integer.parseInt(property);
		} catch (NumberFormatException e) {
			result = defaultValue;
		}
		return result;
	}

	@Override
	public double getProperty(String key, double defaultValue)
	{
		String property = properties.getProperty(key);
		if (property == null) {
			return defaultValue;
		}

		double result;
		try {
			result = Double.parseDouble(property);
		} catch (NumberFormatException e) {
			result = defaultValue;
		}
		return result;
	}

	@Override
	public boolean getProperty(String key, boolean defaultValue)
	{
		String property = properties.getProperty(key);
		if (property == null) {
			return defaultValue;
		}

		if (property.equalsIgnoreCase("true")) {
			return true;
		} else if (property.equalsIgnoreCase("false")) {
			return false;
		}
		return defaultValue;
	}

	@Override
	public <T extends Enum<T>> T getProperty(String key, Class<T> enumType, T defaultValue)
	{
		String property = properties.getProperty(key);
		if (property == null) {
			return defaultValue;
		}

		try {
			return Enum.valueOf(enumType, property);
		} catch (IllegalArgumentException e) {
			// backwards compatibility in case enum values got renamed
			return defaultValue;
		}
	}

	@Override
	public Object get(String key)
	{
		return properties.get(key);
	}

	@Override
	public void setProperty(String key, String value)
	{
		properties.setProperty(key, value);
	}

	@Override
	public void setProperty(String key, int value)
	{
		properties.setProperty(key, Integer.toString(value));
	}

	@Override
	public void setProperty(String key, double value)
	{
		properties.setProperty(key, Double.toString(value));
	}

	@Override
	public void setProperty(String key, boolean value)
	{
		properties.setProperty(key, Boolean.toString(value));
	}

	public Properties getProperties()
	{
		return properties;
	}
}