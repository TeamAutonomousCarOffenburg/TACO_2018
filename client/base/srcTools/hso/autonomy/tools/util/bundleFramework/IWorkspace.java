package hso.autonomy.tools.util.bundleFramework;

import java.io.File;
import java.util.Properties;

public interface IWorkspace {
	File getResource(String relativePath);

	String getWorkspacePath();

	void setProperty(String key, String value);

	String getProperty(String key);

	Properties loadProperties(String path);

	boolean saveProperties(Properties properties, String path);

	boolean saveConfigurationObject(String path, Object object);

	Object getConfigurationObject(String path);

	boolean writeObject(Object object, File file);

	Object readObject(File file);
}
