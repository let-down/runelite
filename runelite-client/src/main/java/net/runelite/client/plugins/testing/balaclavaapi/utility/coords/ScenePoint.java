package net.runelite.client.plugins.testing.balaclavaapi.utility.coords;

import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

@Value
public class ScenePoint
{

	int x;
	int y;
	int plane;

	// scene > world
	public WorldPoint toWorld()
	{
		Client client = EthanApi.getClient();
		return new WorldPoint(x + client.getBaseX(), y + client.getBaseY(), plane);
	}

	// scene > world > region
	public RegionPoint toRegion()
	{
		return RegionPoint.fromWorld(toWorld());
	}

	public int distanceTo(ScenePoint other)
	{
		if (other.plane != plane)
		{
			return Integer.MAX_VALUE;
		}

		return (int) Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
	}

	// region > world > scene
	public static ScenePoint fromRegion(RegionPoint regionPoint)
	{
		return fromWorld(regionPoint.toWorld());
	}

	// world > scene
	public static ScenePoint fromWorld(WorldPoint worldPoint)
	{
		Client client = EthanApi.getClient();
		return new ScenePoint(worldPoint.getX() - client.getBaseX(), worldPoint.getY() - client.getBaseY(), 0);
	}
}
