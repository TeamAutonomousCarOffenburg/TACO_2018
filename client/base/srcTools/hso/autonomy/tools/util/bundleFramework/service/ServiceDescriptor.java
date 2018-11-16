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

public class ServiceDescriptor implements IServiceDescriptor
{
	private final String serviceID;

	private final String[] dependentServices;

	private final Class<?> serviceInterface;

	public ServiceDescriptor(String serviceID, String[] dependentServices, Class<?> serviceInterface)
	{
		this.serviceID = serviceID;
		this.dependentServices = dependentServices;
		this.serviceInterface = serviceInterface;
	}

	@Override
	public String getServiceID()
	{
		return serviceID;
	}

	@Override
	public String[] getDependentServices()
	{
		return dependentServices;
	}

	@Override
	public Class<?> getServiceInterface()
	{
		return serviceInterface;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Service: \"").append(serviceID).append("\" -> \"").append(serviceInterface).append("\"\n");
		sb.append("Dependencies: ").append(dependentServices).append("\n");

		return sb.toString();
	}
}
