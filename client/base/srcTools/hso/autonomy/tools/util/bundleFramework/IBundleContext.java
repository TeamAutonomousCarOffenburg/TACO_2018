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

import java.util.List;
import java.util.Properties;

import hso.autonomy.tools.util.bundleFramework.extension.ExtensionHandle;
import hso.autonomy.tools.util.bundleFramework.service.ServiceHandle;

public interface IBundleContext {
	IBundleDescriptor getDescriptor();

	boolean isValid();

	void setBundleFactory(IBundleFactory bundleFactory);

	<T> ServiceHandle<T> getServiceHandleFor(String serviceID, Class<T> type);

	<T> List<ExtensionHandle<T>> getExtensionHandlesFor(String extensionPointID, Class<T> type);

	void setProperty(String key, String value);

	String getProperty(String key);

	Properties getBundleProperties();

	boolean saveBundleProperties();

	boolean saveBundleProperties(Properties properties);

	Object getConfigurationObject(String path);

	boolean saveConfigurationObject(String path, Object object);

	<T> List<ExtensionHandle<T>> getExtensions(String extensionPointID, Class<T> type);

	<T> ServiceHandle<T> getService(String serviceID, Class<T> type);

	void requestSystemExit();
}
