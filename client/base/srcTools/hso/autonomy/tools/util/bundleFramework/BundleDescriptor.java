package hso.autonomy.tools.util.bundleFramework;

import java.util.List;
import java.util.stream.Collectors;

import hso.autonomy.tools.util.bundleFramework.extension.ExtensionDescriptor;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionPointDescriptor;
import hso.autonomy.tools.util.bundleFramework.service.ServiceDescriptor;

public class BundleDescriptor implements IBundleDescriptor
{
	protected final String name;

	protected final List<ServiceDescriptor> services;

	protected final List<ExtensionPointDescriptor> extensionPoints;

	protected final List<ExtensionDescriptor> extensions;

	public BundleDescriptor(String name, List<ServiceDescriptor> services,
			List<ExtensionPointDescriptor> extensionPoints, List<ExtensionDescriptor> extensions)
	{
		this.name = name;
		this.services = services;
		this.extensionPoints = extensionPoints;
		this.extensions = extensions;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public List<ServiceDescriptor> getServices()
	{
		return services;
	}

	@Override
	public List<ExtensionPointDescriptor> getExtensionPoints()
	{
		return extensionPoints;
	}

	@Override
	public List<ExtensionDescriptor> getExtensions()
	{
		return extensions;
	}

	@Override
	public ServiceDescriptor getServiceFor(String serviceID)
	{
		for (ServiceDescriptor service : services) {
			if (service.getServiceID().equals(serviceID)) {
				return service;
			}
		}

		return null;
	}

	@Override
	public List<ExtensionDescriptor> getExtensionsFor(String extensionPointID)
	{
		return extensions.stream()
				.filter(d -> d.getExtensionPointID().equals(extensionPointID))
				.collect(Collectors.toList());
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("Bundle: ").append(name).append("\n");
		sb.append("Services:\n");
		services.forEach(sb::append);
		sb.append("ExtensionPoints:\n");
		extensionPoints.forEach(sb::append);
		sb.append("Extensions:\n");
		extensions.forEach(sb::append);

		return sb.toString();
	}
}
