package net.runelite.client.plugins.testing.ethanapi.collections;

import net.runelite.client.plugins.testing.ethanapi.collections.query.TileItemQuery;

import java.util.ArrayList;
import java.util.List;

public class TileItems {
    public static List<ETileItem> tileItems = new ArrayList<>();

    public static TileItemQuery search() {
        return new TileItemQuery(tileItems);
    }
}
