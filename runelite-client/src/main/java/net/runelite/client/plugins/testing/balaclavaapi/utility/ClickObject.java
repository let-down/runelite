package net.runelite.client.plugins.testing.balaclavaapi.utility;

import net.runelite.client.plugins.testing.balaclavaapi.utility.coords.Tiles;
import net.runelite.client.plugins.testing.ethanapi.collections.ETileItem;
import net.runelite.client.plugins.testing.ethanapi.collections.query.TileObjectQuery;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;


public class ClickObject {

    public static void objectAction(TileObject object, String... actionlist) {
        if (object == null) {
            System.out.println("null object");
            return;
        }
        ObjectComposition comp = TileObjectQuery.getObjectComposition(object);
        if (comp == null) {
            System.out.println("null comp");

            return;
        }
        if (comp.getActions() == null) {
            System.out.println("null actions");
            return;
        }
        List<String> actions = Arrays.stream(comp.getActions()).collect(Collectors.toList());
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i) == null)
                continue;
            actions.set(i, actions.get(i).toLowerCase());
        }

        int num = -1;
        for (String action : actions) {
            for (String action2 : actionlist) {
                if (action != null && action.equalsIgnoreCase(action2.toLowerCase())) {
                    num = actions.indexOf(action) + 1;
                }
            }
        }

        if (num < 1 || num > 10) {
            return;
        }


        Point clickPoint = DoAction.getClickPoint(object);


        if (object instanceof GameObject) {
            GameObject g = (GameObject) object;
            DoAction.action(g.getSceneMinLocation().getX(), g.getSceneMinLocation().getY(), MenuAction.of(2 + num), g.getId(), -1, "", clickPoint.getX(), clickPoint.getY());
        }else {
            DoAction.action(object.getLocalLocation().getSceneX(), object.getLocalLocation().getSceneY(), MenuAction.of(2+num), object.getId(), -1, "", clickPoint.getX(),clickPoint.getY());
        }

    }

    public static void lootTileItem(ETileItem item) {
        Tile tile = Tiles.getAt(item.getLocation());
        Point clickPoint = DoAction.getClickPoint(item);
        DoAction.action(tile.getSceneLocation().getX(), tile.getSceneLocation().getY(), MenuAction.GROUND_ITEM_THIRD_OPTION, item.getTileItem().getId(), -1, "", clickPoint.getX(), clickPoint.getY());
    }

}
