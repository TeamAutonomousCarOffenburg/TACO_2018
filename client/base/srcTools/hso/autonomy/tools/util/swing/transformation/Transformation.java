package hso.autonomy.tools.util.swing.transformation;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;

import hso.autonomy.tools.util.swing.model.ChangeableSupport;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.Area2D;

/**
 * @author Stefan Glaser
 */
public class Transformation extends ChangeableSupport
{
	/** The shift in x direction as part of the transformation */
	private double xShift;

	/** The shift in y direction as part of the transformation */
	private double yShift;

	/** The scale in x direction as part of the transformation */
	private double xScale;

	/** The scale in y direction as part of the transformation */
	private double yScale;

	/** The dimensions of the panel using this transformation */
	private Dimension panelDim;

	/** The area of the panel used for drawing */
	private Area2D.Int drawArea;

	/**
	 * The shift in pixel on the panel (defines the general position of the
	 * origin on the panel)
	 */
	private Point panelShift;

	/**
	 * The factor defining the relative position shift of the origin in pixel on
	 * the panel in x direction (used to define the panelShift attribute when
	 * updating the panel dimensions)
	 */
	private float xOriginFactor;

	/**
	 * The factor defining the relative position shift of the origin in pixel on
	 * the panel in y direction (used to define the panelShift attribute when
	 * updating the panel dimensions)
	 */
	private float yOriginFactor;

	/** The bounds of this transformation. */
	private TransformationBounds bounds;

	/**
	 * Create a new Transformation.
	 *
	 * @param xOriginFactor - the factor defining the relative position shift of
	 *        the origin in pixel on the panel in x direction
	 * @param yOriginFactor - the factor defining the relative position shift of
	 *        the origin in pixel on the panel in y direction
	 * @param bounds - the transformation bounds
	 */
	public Transformation(float xOriginFactor, float yOriginFactor, TransformationBounds bounds)
	{
		this.xOriginFactor = xOriginFactor;
		this.yOriginFactor = yOriginFactor;
		this.bounds = bounds;

		xShift = 0;
		yShift = 0;
		xScale = 1;
		yScale = 1;

		panelShift = new Point();
		panelDim = new Dimension(10, 10);
		drawArea = new Area2D.Int(0, 10, 0, 10);
	}

	/**
	 * @return the width of the panel
	 */
	public int getPanelWidth()
	{
		return panelDim.width;
	}

	/**
	 * @return the height of the panel
	 */
	public int getPanelHeight()
	{
		return panelDim.height;
	}

	/**
	 * @return the draw area of the panel (the rectangle of the panel used for
	 *         drawing). The draw area is used to determine min/max zoom levels
	 *         in combination with the transformation bounds.
	 */
	public Area2D.Int getDrawArea()
	{
		return drawArea;
	}

	/**
	 * Update the panel dimensions to this transformation. This method is mend to
	 * be called before the panel starts to draw its stuff.
	 *
	 * @param panelWidth - the current panel width
	 * @param panelHeight - the current panel height
	 * @param borderSize - the sizes of the top/left/bottom/right border
	 */
	public void update(int panelWidth, int panelHeight, Insets borderSize)
	{
		int maxX = panelWidth - borderSize.right;
		int maxY = panelHeight - borderSize.bottom;

		if (panelDim.width != panelWidth || panelDim.height != panelHeight ||
				!drawArea.equals(borderSize.left, maxX, borderSize.top, maxY)) {
			panelDim.width = panelWidth;
			panelDim.height = panelHeight;
			drawArea = new Area2D.Int(borderSize.left, maxX, borderSize.top, maxY);
			panelShift.x = borderSize.left + (int) (drawArea.getWidth() * xOriginFactor);
			panelShift.y = borderSize.top + (int) (drawArea.getHeight() * yOriginFactor);

			fireStateChanged();
		}
	}

	/**
	 * @return the translation in x direction of this transformation
	 */
	public double getXTranslation()
	{
		return xShift;
	}

	/**
	 * @return the translation in y direction of this transformation
	 */
	public double getYTranslation()
	{
		return yShift;
	}

	/**
	 * Set the translation of this transformation. The transformation defines
	 * shift in x and y direction in units (not pixel).
	 *
	 * @param xShift - the shift in x direction
	 * @param yShift - the shift in y direction
	 */
	public void setTranslation(double xShift, double yShift)
	{
		this.xShift = xShift;
		this.yShift = yShift;

		limitShiftAndScale();

		fireStateChanged();
	}

	/**
	 * Translate this transformation about the given amount of pixel.
	 *
	 * @param pixelX - the shift in x translation
	 * @param pixelY - the shift in y translation
	 */
	public void translate(int pixelX, int pixelY)
	{
		setTranslation(xShift - xDistance(pixelX), yShift + yDistance(pixelY));
	}

	/**
	 * @return the scale in x direction of this transformation (units/pixel)
	 */
	public double getXScale()
	{
		return xScale;
	}

	/**
	 * @return the scale in y direction of this transformation (units/pixel)
	 */
	public double getYScale()
	{
		return yScale;
	}

	/**
	 * Set the scale of this transformation. The scale factor defines the ratio
	 * pixel/units.
	 *
	 * @param xScale - the scale in x direction
	 * @param yScale - the scale in y direction
	 */
	public void setScale(double xScale, double yScale)
	{
		this.xScale = xScale;
		this.yScale = yScale;

		limitShiftAndScale();

		fireStateChanged();
	}

	/**
	 * Zoom in/out, respectively multiply x-scale factor by zoomFactor.
	 *
	 * @param pixelX - the x pixel coordinate on which to perform the zoom
	 * @param zoomFactor - scale multiplier: > 0 ==> zoom in; < 0 ==> zoom out
	 */
	public void zoomX(int pixelX, float zoomFactor)
	{
		double oldXPos = xDistance(pixelX - panelShift.x);

		// Perform zoom
		xScale *= zoomFactor;

		// Shift translation according to the zoom position
		double xDiff = xDistance(pixelX - panelShift.x) - oldXPos;
		setTranslation(xShift - xDiff, yShift);

		limitShiftAndScale();

		fireStateChanged();
	}

	/**
	 * Zoom in/out, respectively multiply y-scale factor by zoomFactor.
	 *
	 * @param pixelY - the y pixel coordinate on which to perform the zoom
	 * @param zoomFactor - scale multiplier: > 0 ==> zoom in; < 0 ==> zoom out
	 */
	public void zoomY(int pixelY, float zoomFactor)
	{
		double oldYPos = yDistance(pixelY - panelShift.y);

		// Perform zoom
		yScale *= zoomFactor;

		// Shift translation according to the zoom position
		double yDiff = yDistance(pixelY - panelShift.y) - oldYPos;
		setTranslation(xShift, yShift + yDiff);

		limitShiftAndScale();

		fireStateChanged();
	}

	/**
	 * Zoom in/out, respectively multiply x- and y-scale factor by zoomFactor
	 * equally.
	 *
	 * @param pixelPoint - the pixel coordinates on which to perform the zoom
	 * @param zoomFactor - scale multiplier: > 0 ==> zoom in; < 0 ==> zoom out
	 */
	public void zoom(Point pixelPoint, float zoomFactor)
	{
		double oldXPos = xDistance(pixelPoint.x - panelShift.x);
		double oldYPos = yDistance(pixelPoint.y - panelShift.y);

		// Perform zoom
		xScale *= zoomFactor;
		yScale *= zoomFactor;

		// Shift translation according to the zoom position
		double xDiff = xDistance(pixelPoint.x - panelShift.x) - oldXPos;
		double yDiff = yDistance(pixelPoint.y - panelShift.y) - oldYPos;
		setTranslation(xShift - xDiff, yShift + yDiff);

		limitShiftAndScale();

		fireStateChanged();
	}

	public void zoomWindow(Vector2D p1, Vector2D p2)
	{
		double minX = p1.getX();
		double maxX = p2.getX();
		double minY = p1.getY();
		double maxY = p2.getY();

		if (minX > maxX) {
			minX = maxX;
			maxX = p1.getX();
		}

		if (minY > maxY) {
			minY = maxY;
			maxY = p1.getY();
		}

		if (minX > bounds.getMaxX() || maxX < bounds.getMinX() || minY > bounds.getMaxY() || maxY < bounds.getMinY()) {
			// Zoom window is outside bounds
			return;
		}

		// Limit zoom window to x bounds
		if (minX < bounds.getMinX()) {
			minX = bounds.getMinX();
		}
		if (maxX > bounds.getMaxX()) {
			maxX = bounds.getMaxX();
		}

		// Limit zoom window to y bounds
		if (minY < bounds.getMinY()) {
			minY = bounds.getMinY();
		}
		if (maxY > bounds.getMaxY()) {
			maxY = bounds.getMaxY();
		}

		double zoomWidth = maxX - minX;
		double zoomHeight = maxY - minY;

		xScale = drawArea.getWidth() / zoomWidth;
		yScale = drawArea.getHeight() / zoomHeight;
		xShift = minX + zoomWidth * xOriginFactor;
		yShift = minY + zoomHeight * yOriginFactor;

		fireStateChanged();
	}

	private void limitShiftAndScale()
	{
		double drawWidth = xDistance(drawArea.getWidth());
		double drawLeft = drawWidth * xOriginFactor;
		double drawRight = drawWidth * (1 - xOriginFactor);
		double maxWidth = bounds.getMaxX() - bounds.getMinX();

		double drawHeight = yDistance(drawArea.getHeight());
		double drawTop = drawHeight * yOriginFactor;
		double drawBottom = drawHeight * (1 - yOriginFactor);
		double maxHeight = bounds.getMaxY() - bounds.getMinY();

		if (drawWidth >= maxWidth) {
			// the panel is wider than the allowed x range
			xShift = bounds.getMinX() + maxWidth * xOriginFactor;
			xScale = drawArea.getWidth() / maxWidth;
		} else if (xShift + drawRight > bounds.getMaxX()) {
			xShift = bounds.getMaxX() - drawRight;
		} else if (xShift - drawLeft < bounds.getMinX()) {
			xShift = bounds.getMinX() + drawLeft;
		}

		if (drawHeight >= maxHeight) {
			// the panel is higher than the allowed y range
			yShift = bounds.getMinY() + maxHeight * yOriginFactor;
			yScale = drawArea.getHeight() / maxHeight;
		} else if (yShift + drawBottom > bounds.getMaxY()) {
			yShift = bounds.getMaxY() - drawBottom;
		} else if (yShift - drawTop < bounds.getMinY()) {
			yShift = bounds.getMinY() + drawTop;
		}
	}

	/**
	 * Transform the given pixel to a point in user-space-coordinates.
	 *
	 * @param pixelPoint - the pixel position
	 * @return the point in user-space-coordinates
	 */
	public Vector2D pixelToPoint(Point pixelPoint)
	{
		return new Vector2D(pixelToPointX(pixelPoint.x), pixelToPointY(pixelPoint.y));
	}

	/**
	 * Transform the given point in user space to a pixel relative to the panel.
	 *
	 * @param point - the point in user-space-coordinates
	 * @return the pixel relative to the panel
	 */
	public Point pointToPixel(Vector2D point)
	{
		return new Point(pointToPixelX((float) point.getX()), pointToPixelY((float) point.getY()));
	}

	/**
	 * Transform the given x pixel-coordinate to a x coordinate in user-space.
	 *
	 * @param pixelX - the x coordinate of the pixel
	 * @return the x coordinate in user-space
	 */
	public double pixelToPointX(int pixelX)
	{
		return (pixelX - panelShift.x) / xScale + xShift;
	}

	/**
	 * Transform the given y pixel-coordinate to a y coordinate in user-space.
	 *
	 * @param pixelY - the y coordinate of the pixel
	 * @return the y coordinate in user-space
	 */
	public double pixelToPointY(int pixelY)
	{
		return -1 * (pixelY - panelShift.y) / yScale + yShift;
	}

	/**
	 * Transform the given pixel distance in x direction into a distance in the
	 * user-space in x direction.
	 *
	 * @param pixelDistance - the pixel distance in x direction
	 * @return the distance in user-space
	 */
	public double xDistance(int pixelDistance)
	{
		return pixelDistance / xScale;
	}

	/**
	 * Transform the given pixel distance in y direction into a distance in the
	 * user-space in y direction.
	 *
	 * @param pixelDistance - the pixel distance in y direction
	 * @return the distance in user-space
	 */
	public double yDistance(int pixelDistance)
	{
		return pixelDistance / yScale;
	}

	/**
	 * Transform the given x coordinate in user space to a pixel coordinate
	 * relative to the panel.
	 *
	 * @param x - the x coordinate in user-space
	 * @return the pixel relative to the panel
	 */
	public int pointToPixelX(double x)
	{
		return (int) (xScale * (x - xShift)) + panelShift.x;
	}

	/**
	 * Transform the given y coordinate in user space to a pixel coordinate
	 * relative to the panel.
	 *
	 * @param y - the y coordinate in user-space
	 * @return the pixel relative to the panel
	 */
	public int pointToPixelY(double y)
	{
		return -1 * (int) (yScale * (y - yShift)) + panelShift.y;
	}

	/**
	 * Transform the given distance in user-space in x direction into a distance
	 * in the pixel-space in x direction.
	 *
	 * @param xDistance - the distance in user-space in x direction
	 * @return the distance in pixel-space
	 */
	public int pixelDistanceX(double xDistance)
	{
		return (int) (xDistance * xScale);
	}

	/**
	 * Transform the given distance in user-space in y direction into a distance
	 * in the pixel-space in y direction.
	 *
	 * @param yDistance - the distance in user-space in y direction
	 * @return the distance in pixel-space
	 */
	public int pixelDistanceY(double yDistance)
	{
		return (int) (yDistance * yScale);
	}
}
