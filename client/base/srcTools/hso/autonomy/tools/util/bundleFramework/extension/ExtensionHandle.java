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
package hso.autonomy.tools.util.bundleFramework.extension;

import hso.autonomy.tools.util.bundleFramework.IBundleFactory;

public class ExtensionHandle<T> implements IExtensionDescriptor
{
	protected final IExtensionDescriptor descriptor;

	private IBundleFactory factory;

	protected T instance;

	public ExtensionHandle(IExtensionDescriptor extensionDescriptor, IBundleFactory factory)
	{
		this.descriptor = extensionDescriptor;
		this.factory = factory;
	}

	@Override
	public String getExtensionID()
	{
		return descriptor.getExtensionID();
	}

	@Override
	public String[] getDependentServices()
	{
		return descriptor.getDependentServices();
	}

	@Override
	public String getExtensionPointID()
	{
		return descriptor.getExtensionPointID();
	}

	@Override
	public Class<?> getExtensionPointInterface()
	{
		return descriptor.getExtensionPointInterface();
	}

	public T get()
	{
		if (instance == null) {
			Object o = factory.createExtensionInstanceFor(descriptor.getExtensionID());
			if (descriptor.getExtensionPointInterface().isInstance(o)) {
				instance = (T) o;
			} else {
				System.err.println("Extension instance isn't proper!");
				System.err.println("Expected: " + descriptor.getExtensionPointInterface());
				System.err.println("But is: " + ((o == null) ? null : o.getClass()));
			}
		}

		return instance;
	}

	@Override
	public String toString()
	{
		return getExtensionID();
	}
}
