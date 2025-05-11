package net.runelite.client.plugins.testing.balaclavaapi.utility.coords;

import net.runelite.api.coords.WorldPoint;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Random;

public class PolygonalArea implements Area
{
	private final Polygon polygon;
	private final int plane;

	public PolygonalArea(int plane, WorldPoint... points)
	{
		this.plane = plane;
		this.polygon = new Polygon();

		for (WorldPoint point : points)
		{
			polygon.addPoint(point.getX(), point.getY());
		}
	}

	@Override
	public boolean contains(WorldPoint worldPoint)
	{
		if (worldPoint.getPlane() == -1 || worldPoint.getPlane() != plane)
		{
			return false;
		}

		return polygon.contains(worldPoint.getX(), worldPoint.getY());
	}

	@Override
	public WorldPoint getRandomTile()
	{
		int x, y;
		Rectangle r = polygon.getBounds();
		do
		{
			Random random = new Random();
			 x = random.nextInt((r.x + r.width - r.x) + 1) + r.x;
			 y = random.nextInt((r.y + r.height - r.y) + 1) + r.y;

		} while (!polygon.contains(x, y));
		return new WorldPoint(x, y, plane);
	}
}
