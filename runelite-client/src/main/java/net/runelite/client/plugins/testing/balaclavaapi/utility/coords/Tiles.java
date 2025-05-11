package net.runelite.client.plugins.testing.balaclavaapi.utility.coords;

import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Tiles
{
    public static List<Tile> getAll(Predicate<Tile> filter)
    {
        List<Tile> out = new ArrayList<>();

        for (int x = 0; x < Constants.SCENE_SIZE; x++)
        {
            for (int y = 0; y < Constants.SCENE_SIZE; y++)
            {
                Tile tile = EthanApi.getClient().getScene().getTiles()[EthanApi.getClient().getPlane()][x][y];
                if (tile != null && filter.test(tile))
                {
                    out.add(tile);
                }
            }
        }

        return out;
    }

    public static List<Tile> getAll()
    {
        return getAll(x -> true);
    }

    public static Tile getAt(WorldPoint worldPoint)
    {
        return getAt(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }

    public static Tile getAt(LocalPoint localPoint)
    {
        return EthanApi.getClient().getScene().getTiles()[EthanApi.getClient().getPlane()][localPoint.getSceneX()][localPoint.getSceneY()];
    }

    public static Tile getAt(int worldX, int worldY, int plane)
    {
        Client client = EthanApi.getClient();
        int correctedX = worldX < Constants.SCENE_SIZE ? worldX + client.getBaseX() : worldX;
        int correctedY = worldY < Constants.SCENE_SIZE ? worldY + client.getBaseY() : worldY;

        if (!WorldPoint.isInScene(client, correctedX, correctedY))
        {
            return null;
        }

        int x = correctedX - client.getBaseX();
        int y = correctedY - client.getBaseY();

        return client.getScene().getTiles()[plane][x][y];
    }

    public static Tile getAt(RegionPoint regionPoint)
    {
        return getAt(regionPoint.toWorld());
    }

    public static Tile getAt(ScenePoint scenePoint)
    {
        return EthanApi.getClient().getScene().getTiles()[scenePoint.getPlane()][scenePoint.getX()][scenePoint.getY()];
    }

    public static List<Tile> getSurrounding(WorldPoint worldPoint, int radius)
    {
        List<Tile> out = new ArrayList<>();
        for (int x = -radius; x <= radius; x++)
        {
            for (int y = -radius; y <= radius; y++)
            {
                out.add(getAt(worldPoint.dx(x).dy(y)));
            }
        }

        return out;
    }

    public static Tile getHoveredTile()
    {
        return EthanApi.getClient().getSelectedSceneTile();
    }


}