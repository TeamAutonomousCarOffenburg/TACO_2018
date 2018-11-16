/*******************************************************************************
 * Copyright 2008, 2012 Hochschule Offenburg
 * Klaus Dorer, Mathias Ehret, Stefan Glaser, Thomas Huber, Fabian Korak,
 * Simon Raffeiner, Srinivasa Ragavan, Thomas Rinklin,
 * Joachim Schilling, Ingo Schindler, Rajit Shahi, Bjoern Weiler
 *
 * This file is part of magmaOffenburg.
 *
 * magmaOffenburg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * magmaOffenburg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with magmaOffenburg. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package hso.autonomy.tools.util.bundleFramework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Workspace implements IWorkspace
{
	protected final XStream xStream;

	protected final String wsPath;

	protected Properties wsProperties;

	public Workspace()
	{
		this(null);
	}

	public Workspace(String wsPath)
	{
		if (wsPath == null) {
			this.wsPath = System.getProperty("user.home") + "/.magmaDeveloper";
		} else {
			this.wsPath = wsPath;
		}

		xStream = new XStream(new DomDriver());
	}

	@Override
	public File getResource(String relativePath)
	{
		return new File(wsPath + relativePath);
	}

	@Override
	public String getWorkspacePath()
	{
		return wsPath;
	}

	@Override
	public void setProperty(String key, String value)
	{
		wsProperties.setProperty(key, value);
	}

	@Override
	public String getProperty(String key)
	{
		return wsProperties.getProperty(key);
	}

	@Override
	public Properties loadProperties(String path)
	{
		return loadPropertiesFile(path);
	}

	@Override
	public boolean saveProperties(Properties properties, String path)
	{
		return savePropertiesFile(properties, path);
	}

	@Override
	public boolean saveConfigurationObject(String path, Object object)
	{
		String xml = xStream.toXML(object);

		File file = new File(wsPath + path);

		FileWriter out;
		try {
			File parentDir = file.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}

			if (!file.exists()) {
				file.createNewFile();
			}

			out = new FileWriter(file);

			out.write(xml);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public Object getConfigurationObject(String path)
	{
		Object obj = null;
		FileReader in;

		try {
			in = new FileReader(wsPath + path);

			obj = xStream.fromXML(in);

			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("INFO: Configuration file: \"" + wsPath + path + "\" not found.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XStreamException e) {
			System.err.println(
					"INFO: Error while parsing configuration file: " + path + ". Trying to delete config file.");

			File file = new File(wsPath + path);
			if (file.exists()) {
				try {
					file.delete();
				} catch (Exception e2) {
				}
			}
			// e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}

	@Override
	public boolean writeObject(Object object, File file)
	{
		String xml = xStream.toXML(object);

		FileWriter out;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			out = new FileWriter(file);

			out.write(xml);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public Object readObject(File file)
	{
		Object obj = null;
		FileReader in;

		try {
			in = new FileReader(file);

			obj = xStream.fromXML(in);

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}

	boolean saveWorkspace()
	{
		savePropertiesFile(wsProperties, "/workspace.properties");

		return true;
	}

	boolean loadWorkspace()
	{
		// Create workspace directory if necessary
		File wsDir = new File(wsPath);
		if (!wsDir.exists()) {
			wsDir.mkdirs();
		}

		// load workspace config (properties)
		wsProperties = loadPropertiesFile("/workspace.properties");

		return true;
	}

	private boolean savePropertiesFile(Properties properties, String path)
	{
		try {
			File file = new File(wsPath + path);
			File folder = file.getParentFile();

			if (folder == null || !folder.exists()) {
				folder.mkdirs();
			}

			if (!file.exists()) {
				file.createNewFile();
			}

			properties.store(new FileOutputStream(file), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private Properties loadPropertiesFile(String path)
	{
		Properties properties = new Properties();

		File file = new File(wsPath + path);
		try {
			properties.load(new FileInputStream(file));
			return properties;
		} catch (FileNotFoundException e) {
			System.err.println("INFO: No peroperties file found at \"" + wsPath + path + "\".");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return properties;
	}
}
