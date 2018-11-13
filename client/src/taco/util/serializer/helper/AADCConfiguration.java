package taco.util.serializer.helper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Representation of the roadsign.xml file, defined by Audi
 */
@XmlRootElement(name = "configuration")
public class AADCConfiguration
{
	@XmlElement(name = "roadSign")
	private List<AADCRoadsign> roadSigns;

	public AADCConfiguration()
	{
	}

	public AADCConfiguration(List<AADCRoadsign> roadSigns)
	{
		this.roadSigns = roadSigns;
	}

	public List<AADCRoadsign> getRoadSigns()
	{
		return roadSigns;
	}
}
