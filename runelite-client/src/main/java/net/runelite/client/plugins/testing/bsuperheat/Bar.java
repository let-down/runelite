package net.runelite.client.plugins.testing.bsuperheat;

import net.runelite.api.ItemID;

class ItemWithAmount
{
    private final int itemId;
    private final int amount;

    public ItemWithAmount(int itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    public int getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }
}

public enum Bar {
    BRONZE_BAR(ItemID.BRONZE_BAR, 2, new ItemWithAmount(ItemID.COPPER_ORE,1),new ItemWithAmount(ItemID.TIN_ORE,1)),
    IRON_BAR(ItemID.IRON_BAR, 1, new ItemWithAmount(ItemID.IRON_ORE, 1)),
    SILVER_BAR(ItemID.SILVER_BAR, 1, new ItemWithAmount(ItemID.SILVER_ORE, 1)),
    STEEL_BAR(ItemID.STEEL_BAR, 3, new ItemWithAmount(ItemID.IRON_ORE,1),new ItemWithAmount(ItemID.COAL,2)),
    GOLD_BAR(ItemID.GOLD_BAR, 1, new ItemWithAmount(ItemID.GOLD_ORE, 1)),
    MITHRIL_BAR(ItemID.MITHRIL_BAR, 5, new ItemWithAmount(ItemID.MITHRIL_ORE,1),new ItemWithAmount(ItemID.COAL,4)),
    ADAMANTITE_BAR(ItemID.ADAMANTITE_BAR, 7, new ItemWithAmount(ItemID.ADAMANTITE_ORE,1),new ItemWithAmount(ItemID.COAL,6)),
    RUNITE_BAR(ItemID.RUNITE_BAR, 9, new ItemWithAmount(ItemID.RUNITE_ORE,1),new ItemWithAmount(ItemID.COAL,8));





    private final ItemWithAmount[] items;
    private final int product;

    private final int totalItems;

    Bar(int product, int totalItems, ItemWithAmount... items) {
        this.items = items;
        this.totalItems = totalItems;
        this.product = product;
    }

    public ItemWithAmount[] getItems() {
        return items;
    }

    public int getProduct(){
        return product;
    }

    public int getTotalItems(){
        return totalItems;
    }
}
