package hso.autonomy.tools.util.bundleFramework.service;

import hso.autonomy.tools.util.bundleFramework.IBundleContext;

public class ServiceReference<T>
{
	private final String serviceID;

	private final Class<T> serviceInterface;

	public ServiceReference(String serviceID, Class<T> serviceInterface)
	{
		this.serviceID = serviceID;
		this.serviceInterface = serviceInterface;
	}

	public T get(IBundleContext context)
	{
		return context.getService(serviceID, serviceInterface).get();
	}
}
