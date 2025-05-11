package net.runelite.client.plugins.testing.balaclavaapi.utility;

import net.runelite.client.plugins.testing.balaclavaapi.utility.DoAction;
import net.runelite.client.plugins.testing.balaclavaapi.utility.coords.Tiles;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;

import java.util.Comparator;

public class Walk {
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    public static void to(WorldPoint worldPoint)
    {
        Player local = client.getLocalPlayer();
        if (local == null)
        {
            return;
        }

        WorldPoint walkPoint = worldPoint;
        Tile destinationTile = Tiles.getAt(worldPoint);
        // Check if tile is in loaded client scene
        if (destinationTile == null)
        {
            System.out.print("Destination {} is not in scene");
            Tile nearestInScene = Tiles.getAll()
                    .stream()
                    .min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(local.getWorldLocation())))
                    .orElse(null);
            if (nearestInScene == null)
            {
                System.out.println("Couldn't find nearest walkable tile");
                return;
            }

            walkPoint = nearestInScene.getWorldLocation();
        }

        int sceneX = walkPoint.getX() - client.getTopLevelWorldView().getBaseX();
        int sceneY = walkPoint.getY() - client.getTopLevelWorldView().getBaseY();
        Point canv = Perspective.localToCanvas(client, LocalPoint.fromScene(sceneX, sceneY,client.getTopLevelWorldView().getScene()), client.getTopLevelWorldView().getPlane());
        int x = canv != null ? canv.getX() : 0;
        int y = canv != null ? canv.getY() : 0;

        DoAction.action(
                x,
                y,
                MenuAction.WALK,
                0,
                -1,
                "",
                x,
                y
        );
    }
}
