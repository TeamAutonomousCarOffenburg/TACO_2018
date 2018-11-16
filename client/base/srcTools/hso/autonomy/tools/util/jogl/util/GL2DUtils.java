package hso.autonomy.tools.util.jogl.util;

import java.awt.Color;
import java.awt.Point;

import javax.media.opengl.GL2;

import org.apache.commons.math3.geometry.euclidean.threed.Segment;
import org.apache.commons.math3.geometry.euclidean.threed.SubLine;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;

import hso.autonomy.util.geometry.Arc2D;
import hso.autonomy.util.geometry.Area2D;
import hso.autonomy.util.geometry.Circle2D;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Polygon;

/**
 * Some openGL utils collection (mostly for testing)
 *
 * @author Stefan Glaser
 */
public abstract class GL2DUtils
{
	public static void setClearColor(GL2 gl, Color color)
	{
		gl.glClearColor(
				color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
	}

	public static void setClearColor(GL2 gl, float red, float green, float blue, float alpha)
	{
		gl.glClearColor(red, green, blue, alpha);
	}

	public static void setColor(GL2 gl, Color color)
	{
		setColor(gl, color, color.getAlpha());
	}

	public static void setColor(GL2 gl, Color color, int alpha)
	{
		gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha / 255f);
	}

	public static void setColor(GL2 gl, float red, float green, float blue, float alpha)
	{
		gl.glColor4f(red, green, blue, alpha);
	}

	public static void setColor(GL2 gl, float red, float green, float blue)
	{
		gl.glColor3f(red, green, blue);
	}

	public static void setPointSize(GL2 gl, float size)
	{
		gl.glPointSize(size);
	}

	public static void drawPoint(GL2 gl, double x, double y, Color color, double size)
	{
		double halfSize = size / 2.0;
		drawRect(gl, x - halfSize, y - halfSize, x + halfSize, y + halfSize, color, color);
	}

	public static void drawPoint(GL2 gl, Vector2D vector, Color color, double size)
	{
		drawPoint(gl, vector.getX(), vector.getY(), color, size);
	}

	public static void drawPose(GL2 gl, IPose3D pose, Color color, double size)
	{
		withTransformation(gl, pose, () -> {
			setColor(gl, color);
			float halfSize = (float) (size / 2);
			drawTriangle(gl, -halfSize, -halfSize * 2, -halfSize, halfSize * 2, (float) size, 0);
		});
	}

	public static void drawPoint(GL2 gl, Vector3D vector, Color color, double size)
	{
		drawPoint(gl, vector.getX(), vector.getY(), color, size);
	}

	public static void drawPoints(GL2 gl, Point[] points)
	{
		gl.glBegin(GL2.GL_POINTS);
		{
			for (Point point : points) {
				gl.glVertex2i(point.x, point.y);
			}
		}
		gl.glEnd();
	}

	public static void drawLine(GL2 gl, SubLine line, Color color)
	{
		setColor(gl, color);
		Segment segment = line.getSegments().get(0);
		drawLine(gl, segment.getStart(), segment.getEnd());
	}

	public static void drawLine(GL2 gl, int x1, int y1, int x2, int y2)
	{
		gl.glBegin(GL2.GL_LINES);
		{
			gl.glVertex2i(x1, y1);
			gl.glVertex2i(x2, y2);
		}
		gl.glEnd();
	}

	public static void drawLine(GL2 gl, Point p1, Point p2)
	{
		gl.glBegin(GL2.GL_LINES);
		{
			gl.glVertex2i(p1.x, p1.y);
			gl.glVertex2i(p2.x, p2.y);
		}
		gl.glEnd();
	}

	public static void drawLine(GL2 gl, Vector3D p1, Vector3D p2)
	{
		gl.glBegin(GL2.GL_LINES);
		{
			gl.glVertex2d(p1.getX(), p1.getY());
			gl.glVertex2d(p2.getX(), p2.getY());
		}
		gl.glEnd();
	}

	public static void drawLines(GL2 gl, Point[] points)
	{
		gl.glBegin(GL2.GL_LINES);
		{
			for (int i = 0; i < points.length - 1; i += 2) {
				gl.glVertex2i(points[i].x, points[i].y);
				gl.glVertex2i(points[i + 1].x, points[i + 1].y);
			}
		}
		gl.glEnd();
	}

	public static void drawLineStrip(GL2 gl, Point[] points)
	{
		gl.glBegin(GL2.GL_LINE_STRIP);
		{
			for (Point point : points) {
				gl.glVertex2i(point.x, point.y);
			}
		}
		gl.glEnd();
	}

	public static void drawLineStrip(GL2 gl, float[] points)
	{
		gl.glBegin(GL2.GL_LINE_STRIP);
		{
			for (int i = 0; i < points.length - 1; i += 2) {
				gl.glVertex2f(points[i], points[i + 1]);
			}
		}
		gl.glEnd();
	}

	public static void drawLineLoop(GL2 gl, Point[] points)
	{
		gl.glBegin(GL2.GL_LINE_LOOP);
		{
			for (Point point : points) {
				gl.glVertex2i(point.x, point.y);
			}
		}
		gl.glEnd();
	}

	public static void drawTriangle(GL2 gl, Point p1, Point p2, Point p3)
	{
		gl.glBegin(GL2.GL_TRIANGLES);
		{
			gl.glVertex2i(p1.x, p1.y);
			gl.glVertex2i(p2.x, p2.y);
			gl.glVertex2i(p3.x, p3.y);
		}
		gl.glEnd();
	}

	public static void drawTriangle(GL2 gl, float x1, float y1, float x2, float y2, float x3, float y3)
	{
		gl.glBegin(GL2.GL_TRIANGLES);
		{
			gl.glVertex2f(x1, y1);
			gl.glVertex2f(x2, y2);
			gl.glVertex2f(x3, y3);
		}
		gl.glEnd();
	}

	public static void drawTriangles(GL2 gl, Point[] points)
	{
		gl.glBegin(GL2.GL_TRIANGLES);
		{
			for (int i = 0; i < points.length - 2; i += 3) {
				gl.glVertex2i(points[i].x, points[i].y);
				gl.glVertex2i(points[i + 1].x, points[i + 1].y);
				gl.glVertex2i(points[i + 2].x, points[i + 2].y);
			}
		}
		gl.glEnd();
	}

	public static void drawTriangleStrip(GL2 gl, Point[] points)
	{
		gl.glBegin(GL2.GL_TRIANGLE_STRIP);
		for (Point point : points) {
			gl.glVertex2i(point.x, point.y);
		}
		gl.glEnd();
	}

	public static void drawTriangleFan(GL2 gl, Point[] points)
	{
		gl.glBegin(GL2.GL_TRIANGLE_FAN);
		for (Point point : points) {
			gl.glVertex2i(point.x, point.y);
		}
		gl.glEnd();
	}

	public static void drawRect(GL2 gl, Point p1, Point p2)
	{
		gl.glRecti(p1.x, p1.y, p2.x, p2.y);
	}

	public static void drawUnfilledRect(GL2 gl, double x1, double y1, double x2, double y2)
	{
		gl.glBegin(GL2.GL_LINE_STRIP);
		{
			gl.glVertex2d(x1, y1);
			gl.glVertex2d(x2, y1);
			gl.glVertex2d(x2, y2);
			gl.glVertex2d(x1, y2);
			gl.glVertex2d(x1, y1);
		}
		gl.glEnd();
	}

	public static void drawRect(GL2 gl, double x1, double y1, double x2, double y2, Color color, Color borderColor)
	{
		setColor(gl, color);
		gl.glRectd(x1, y1, x2, y2);

		setColor(gl, borderColor);
		drawUnfilledRect(gl, x1, y1, x2, y2);
	}

	public static void drawRect(GL2 gl, Area2D.Float area, Color color, Color borderColor)
	{
		setColor(gl, color);
		gl.glRectd(area.getMinX(), area.getMinY(), area.getMaxX(), area.getMaxY());

		setColor(gl, borderColor);
		drawUnfilledRect(gl, area.getMinX(), area.getMinY(), area.getMaxX(), area.getMaxY());
	}

	public static void drawPolygon(GL2 gl, Polygon polygon, Color color)
	{
		setColor(gl, color);

		gl.glBegin(GL2.GL_POLYGON);
		for (Vector2D point : polygon.getPoints()) {
			gl.glVertex2d(point.getX(), point.getY());
		}
		gl.glEnd();
	}

	public static void fillCircle(GL2 gl, float x, float y, float radius, final int segments)
	{
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		{
			gl.glTranslatef(x, y, 0.0f);
			float angleInc = 2.0f * 3.1416f / segments;

			gl.glBegin(GL2.GL_POLYGON);
			gl.glVertex2f(radius, 0);
			for (float angle = 0; angle < 2 * Math.PI; angle += angleInc) {
				gl.glVertex2f((float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)));
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
	}

	public static void drawCircle(GL2 gl, float x, float y, float radius, Color color, final int segments)
	{
		drawArc(gl, x, y, radius, color, segments, 0, 2 * Math.PI, GL2.GL_LINE_LOOP);
	}

	public static void drawArc(GL2 gl, Arc2D arc, Color color, final int segments)
	{
		drawArc(gl, (float) arc.getX(), (float) arc.getY(), (float) arc.getRadius(), color, segments,
				arc.getStartAngle().radiansPositive(), arc.getEndAngle().radiansPositive(), GL2.GL_LINE_STRIP);
	}

	public static void drawArc(GL2 gl, float x, float y, float radius, Color color, final int segments,
			double startAngle, double endAngle, int mode)
	{
		GL2DUtils.setColor(gl, color);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		{
			gl.glTranslatef(x, y, 0.0f);
			if (endAngle < startAngle) {
				endAngle += 2 * Math.PI;
			}
			double angleInc = (endAngle - startAngle) / segments;
			gl.glBegin(mode);
			for (double angle = startAngle; angle <= endAngle; angle += angleInc) {
				gl.glVertex2f((float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)));
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
	}

	public static void drawCircle(GL2 gl, Circle2D circle, Color color, final int segments)
	{
		drawCircle(gl, (float) circle.getX(), (float) circle.getY(), (float) circle.getRadius(), color, segments);
	}

	public static void fillCircle(GL2 gl, float radius, final int segments)
	{
		float angleInc = 2.0f * 3.1416f / segments;

		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex2f(radius, 0);
		for (float angle = 0; angle < 2 * Math.PI; angle += angleInc) {
			gl.glVertex2f((float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)));
		}
		gl.glEnd();
	}

	public static void drawCircle(GL2 gl, float radius, Color color, final int segments)
	{
		GL2DUtils.setColor(gl, color);

		float angleInc = 2.0f * 3.1416f / segments;

		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex2f(radius, 0);
		for (float angle = 0; angle < 2 * Math.PI; angle += angleInc) {
			gl.glVertex2f((float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)));
		}
		gl.glEnd();
	}

	public static void fillArc(GL2 gl, float radius, int startAngle, int endAngle, final int segments)
	{
		float startAngleRad = (float) Math.toRadians(startAngle);
		float endAngleRad = (float) Math.toRadians(endAngle);
		float angleInc = (endAngleRad - startAngleRad) / segments;

		gl.glBegin(GL2.GL_POLYGON);
		{
			gl.glVertex2f(0, 0);

			for (float angle = startAngleRad; angle < endAngleRad; angle += angleInc) {
				gl.glVertex2f((float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)));
			}
		}
		gl.glEnd();
	}

	/**
	 * @param color - the player color
	 */
	public static void drawPlayer(GL2 gl, Color color)
	{
		// Background color
		setColor(gl, Color.BLACK, color.getAlpha());

		fillCircle(gl, 0.2f, 24);
		drawTriangle(gl, 0.23f, 0.12f, 0.45f, 0, 0.23f, -0.12f);

		// Foreground color
		setColor(gl, color);
		fillCircle(gl, 0.15f, 24);

		// Direction indicator color
		setColor(gl, Color.YELLOW, color.getAlpha());
		drawTriangle(gl, 0.26f, 0.07f, 0.4f, 0, 0.26f, -0.07f);
	}

	public static void withTransformation(GL2 gl, IPose3D pose, Runnable f)
	{
		gl.glPushMatrix();
		{
			gl.glTranslated(pose.getX(), pose.getY(), pose.getZ());
			// TODO 3D use full orientation?
			gl.glRotated(pose.getHorizontalAngle().degrees(), 0, 0, 1);
			f.run();
		}
		gl.glPopMatrix();
	}

	public static void drawTexture(GL2 gl, Texture texture, float size, float width, float height)
	{
		texture.enable(gl);
		texture.bind(gl);

		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glTexCoord2d(0.0, 0.0);
			gl.glVertex2d(0, 0);
			gl.glTexCoord2d(size, 0.0);
			gl.glVertex2d(width, 0);
			gl.glTexCoord2d(size, size);
			gl.glVertex2d(width, height);
			gl.glTexCoord2d(0.0, size);
			gl.glVertex2d(0, height);
		}
		gl.glEnd();

		texture.disable(gl);
	}

	public static void drawText(
			String text, Color color, Vector2D position, float scaleFactor, TextRenderer textRenderer)
	{
		textRenderer.begin3DRendering();
		textRenderer.setColor(color);
		float margin = 0.04f;
		textRenderer.draw3D(text, (float) position.getX() + margin, (float) position.getY() + margin, 0, scaleFactor);
		textRenderer.end3DRendering();
	}
}
