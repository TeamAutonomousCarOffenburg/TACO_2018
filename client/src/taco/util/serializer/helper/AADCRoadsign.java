package taco.util.serializer.helper;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Representation of the roadsign objects inside the roadsign.xml.
 * Defined by Audi
 */
public class AADCRoadsign
{
	@XmlAttribute(name = "id", required = true)
	private int id;

	@XmlAttribute(name = "x", required = true)
	private double x;

	@XmlAttribute(name = "y", required = true)
	private double y;

	@XmlAttribute(name = "radius", required = true)
	private double radius;

	@XmlAttribute(name = "direction", required = true)
	private double direction;

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "init")
	private int init;

	public AADCRoadsign()
	{
	}

	public AADCRoadsign(int id, double x, double y, double radius, double direction)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.direction = direction;
	}

	public int getId()
	{
		return id;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getRadius()
	{
		return radius;
	}

	public double getDirection()
	{
		return direction;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getInit()
	{
		return init;
	}

	public void markAsSectorSign()
	{
		// the signs at the sector beginning have separated ID's. Just for reusing (render or may localizer) we mark
		// them as unknown
		this.id = -2;
	}
}