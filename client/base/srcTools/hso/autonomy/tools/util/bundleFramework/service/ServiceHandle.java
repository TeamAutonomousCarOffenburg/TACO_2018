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
package hso.autonomy.tools.util.bundleFramework.service;

import hso.autonomy.tools.util.bundleFramework.IBundleFactory;

public class ServiceHandle<T> implements IServiceDescriptor
{
	protected final ServiceDescriptor descriptor;

	private IBundleFactory factory;

	protected T instance;

	public ServiceHandle(ServiceDescriptor serviceDescriptor, IBundleFactory factory)
	{
		this.descriptor = serviceDescriptor;
		this.factory = factory;
	}

	@Override
	public String getServiceID()
	{
		return descriptor.getServiceID();
	}

	@Override
	public String[] getDependentServices()
	{
		return descriptor.getDependentServices();
	}

	@Override
	public Class<?> getServiceInterface()
	{
		return descriptor.getServiceInterface();
	}

	public T get()
	{
		if (instance == null) {
			Object o = factory.createServiceInstanceFor(descriptor.getServiceID());
			if (descriptor.getServiceInterface().isInstance(o)) {
				instance = (T) o;
			} else {
				System.err.println("Service Instance isn't proper!");
				System.err.println("Expected: " + descriptor.getServiceInterface());
				System.err.println("But is: " + o.getClass());
			}
		}

		return instance;
	}
}
