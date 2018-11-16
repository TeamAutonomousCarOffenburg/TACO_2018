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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionDescriptor;
import hso.autonomy.tools.util.bundleFramework.service.ServiceDescriptor;
import hso.autonomy.tools.util.bundleFramework.service.ServiceHandle;

public final class BundleContext implements IBundleContext, IWorkspace
{
	final Bundle bundle;

	private final BundleManager bundleManager;

	private final IWorkspace ws;

	private IBundleFactory factory;

	final Map<String, ServiceHandle<?>> serviceHandles;

	Properties bundleProperties;

	BundleContext(Bundle bundle, BundleManager bundleManager, IWorkspace ws)
	{
		this.bundle = bundle;
		this.bundleManager = bundleManager;
		this.ws = ws;
		this.serviceHandles = new HashMap<>();
	}

	@Override
	public IBundleDescriptor getDescriptor()
	{
		return bundle.getBundleDescriptor();
	}

	@Override
	public void setBundleFactory(IBundleFactory bundleFactory)
	{
		this.factory = bundleFactory;
	}

	@Override
	public <T> ServiceHandle<T> getServiceHandleFor(String serviceID, Class<T> type)
	{
		// Check for buffered handle instance
		ServiceHandle<T> serviceHandle = (ServiceHandle<T>) serviceHandles.get(serviceID);

		if (serviceHandle == null) {
			ServiceDescriptor service = bundle.getBundleDescriptor().getServiceFor(serviceID);

			// If no corresponding service was found, return null
			if (service == null) {
				return null;
			} else {
				serviceHandle = new ServiceHandle<>(service, factory);
				serviceHandles.put(serviceID, serviceHandle);
			}
		}

		return serviceHandle;
	}

	@Override
	public <T> List<ExtensionHandle<T>> getExtensionHandlesFor(String extensionPointID, Class<T> type)
	{
		List<ExtensionDescriptor> descriptors = bundle.getBundleDescriptor().getExtensionsFor(extensionPointID);

		List<ExtensionHandle<T>> handles = new ArrayList<>();

		for (ExtensionDescriptor e : descriptors) {
			handles.add(new ExtensionHandle<T>(e, factory));
		}

		return handles;
	}

	@Override
	public boolean isValid()
	{
		return factory != null;
	}

	@Override
	public File getResource(String relativePath)
	{
		return ws.getResource(relativePath);
	}

	@Override
	public String getWorkspacePath()
	{
		return ws.getWorkspacePath();
	}

	@Override
	public void setProperty(String key, String value)
	{
		getBundleProperties().setProperty(key, value);
	}

	@Override
	public String getProperty(String key)
	{
		return getBundleProperties().getProperty(key);
	}

	@Override
	public Properties getBundleProperties()
	{
		if (bundleProperties == null) {
			bundleProperties = loadProperties(bundle.getBundleDescriptor().getName() + ".properties");

			// If there exist no bundle properties, create one
			if (bundleProperties == null) {
				bundleProperties = new Properties();
			}
		}

		return bundleProperties;
	}

	@Override
	public boolean saveBundleProperties()
	{
		return saveBundleProperties(getBundleProperties());
	}

	@Override
	public boolean saveBundleProperties(Properties properties)
	{
		if (bundleProperties != null) {
			saveProperties(bundleProperties, bundle.getBundleDescriptor().getName() + ".properties");
			return true;
		}

		return false;
	}

	@Override
	public Properties loadProperties(String path)
	{
		return ws.loadProperties(File.separator + bundle.getBundleDescriptor().getName() + File.separator + path);
	}

	@Override
	public boolean saveProperties(Properties properties, String path)
	{
		return ws.saveProperties(
				properties, File.separator + bundle.getBundleDescriptor().getName() + File.separator + path);
	}

	@Override
	public Object getConfigurationObject(String path)
	{
		return ws.getConfigurationObject(
				File.separator + bundle.getBundleDescriptor().getName() + File.separator + path);
	}

	@Override
	public boolean saveConfigurationObject(String path, Object object)
	{
		return ws.saveConfigurationObject(
				File.separator + bundle.getBundleDescriptor().getName() + File.separator + path, object);
	}

	@Override
	public boolean writeObject(Object object, File file)
	{
		return ws.writeObject(object, file);
	}

	@Override
	public Object readObject(File file)
	{
		return ws.readObject(file);
	}

	@Override
	public <T> List<ExtensionHandle<T>> getExtensions(String extensionPointID, Class<T> type)
	{
		return bundleManager.getExtensions(extensionPointID, type);
	}

	@Override
	public <T> ServiceHandle<T> getService(String serviceID, Class<T> type)
	{
		return bundleManager.getService(serviceID, type);
	}

	@Override
	public void requestSystemExit()
	{
		bundleManager.shutdown();
	}
}
