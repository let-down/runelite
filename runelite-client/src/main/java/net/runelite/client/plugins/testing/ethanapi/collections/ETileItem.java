package net.runelite.client.plugins.testing.ethanapi.collections;

import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import static net.runelite.api.TileItem.OWNERSHIP_GROUP;
import static net.runelite.api.TileItem.OWNERSHIP_SELF;

public class ETileItem {
    public WorldPoint location;
    public TileItem tileItem;

    public ETileItem(WorldPoint worldLocation, TileItem tileItem) {
        this.location = worldLocation;
        this.tileItem = tileItem;
    }

    public WorldPoint getLocation() {
        return location;
    }

    public TileItem getTileItem() {
        return tileItem;
    }
    public boolean isMine(){
        return tileItem.getOwnership() == OWNERSHIP_SELF||tileItem.getOwnership()==OWNERSHIP_GROUP;
    }

    public void interact(boolean ctrlDown) {
//        MousePackets.queueClickPacket();
//        TileItemPackets.queueTileItemAction(this, ctrlDown);
    }
}
