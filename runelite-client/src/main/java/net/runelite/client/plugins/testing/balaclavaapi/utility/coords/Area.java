package net.runelite.client.plugins.testing.balaclavaapi.utility.coords;

import net.runelite.api.coords.WorldPoint;

import java.util.Arrays;

public interface Area
{
	boolean contains(WorldPoint worldPoint);

	static Area union(Area... areas)
	{
		return point -> Arrays.stream(areas).anyMatch(a -> a.contains(point));
	}

	static Area intersection(Area... areas)
	{
		return point -> Arrays.stream(areas).allMatch(a -> a.contains(point));
	}

	default Area minus(Area other)
	{
		return point -> Area.this.contains(point) && !other.contains(point);
	}

	default WorldPoint getRandomTile()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
