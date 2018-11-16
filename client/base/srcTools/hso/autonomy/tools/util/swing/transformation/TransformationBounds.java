package hso.autonomy.tools.util.swing.transformation;

/**
 * @author Stefan Glaser
 */
public class TransformationBounds
{
	/** The minimum allowed x value */
	private double minX;

	/** The maximum allowed x value */
	private double maxX;

	/** The minimum allowed y value */
	private double minY;

	/** The maximum allowed y value */
	private double maxY;

	/**
	 * @param minX - the minimum allowed x value
	 * @param maxX - the maximum allowed x value
	 * @param minY - the minimum allowed y value
	 * @param maxY - the maximum allowed y value
	 */
	public TransformationBounds(double minX, double maxX, double minY, double maxY)
	{
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}

	public double getMinX()
	{
		return minX;
	}

	public void setMinX(double minX)
	{
		this.minX = minX;
	}

	public double getMaxX()
	{
		return maxX;
	}

	public void setMaxX(double maxX)
	{
		this.maxX = maxX;
	}

	public double getMinY()
	{
		return minY;
	}

	public void setMinY(double minY)
	{
		this.minY = minY;
	}

	public double getMaxY()
	{
		return maxY;
	}

	public void setMaxY(double maxY)
	{
		this.maxY = maxY;
	}
}
