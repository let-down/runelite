package net.runelite.client.plugins.testing.balaclavaapi.interactionutility;

import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.ethanapi.collections.Inventory;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.game.ItemManager;

import java.util.Optional;

public class InventoryUtility {
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    static ItemManager itemManager = RuneLite.getInjector().getInstance(ItemManager.class);

    public static boolean inventoryIsFull() {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            return false;
        } else {
            return inventory.count() == 28;
        }
    }


    public static boolean inventoryContains(int... itemIds) {
        for (int itemId : itemIds) {
            if (Inventory.search().withId(itemId).first().isEmpty()) {
                return false;  // Return false if any itemId is not found
            }
        }
        return true;  // Return true if all itemIds are found
    }

    public static boolean inventoryContains(int itemId, int amount) {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

        if (inventory == null) {
            return false;
        }

        Item[] inventoryItems = inventory.getItems();
        int counter = 0;
        ItemComposition itemComposition = itemManager.getItemComposition(itemId);
        boolean isStackable = itemComposition.isStackable();

        for (Item item : inventoryItems) {
            if (item.getId() == itemId) {
                counter += isStackable ? item.getQuantity() : 1;
            }
            if (counter >= amount) {
                return true;
            }
        }

        return false;
    }

    public static int inventoryAmount(int itemId) {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

        if (inventory == null) {
            return 0;
        }

        Item[] inventoryItems = inventory.getItems();
        int itemCount = 0;
        ItemComposition itemComposition = itemManager.getItemComposition(itemId);
        boolean isStackable = itemComposition.isStackable();

        for (Item item : inventoryItems) {
            if (item.getId() == itemId) {
                itemCount += isStackable ? item.getQuantity() : 1;
            }
        }

        return itemCount;
    }

    public static void use(Widget widget, String... actions){
        if(widget == null){
            return;
        }

        ClickWidget.widgetAction(widget, actions);
    }

    public static void use(int itemId, String... actions){
        Optional<Widget> item = Inventory.search().withId(itemId).first();
        if(item.isEmpty()){
            return;
        }
        ClickWidget.widgetAction(item.get(), actions);
    }

    public static void useOn(Widget widget,NPC npc){
        ClickWidget.widgetOnNpc(widget,npc);
    }

    public static void useOn(int itemId,NPC npc){
        Optional<Widget> item = Inventory.search().withId(itemId).first();
        if(!item.isEmpty()) {
            ClickWidget.widgetOnNpc(item.get(), npc);
        }
    }

    public static boolean hasRune(int runeId, int amount) {
        if (inventoryContains(runeId, amount)) {
            return true;
        } else {
            if (Inventory.search().matchesWildCardNoCase("rune pouch").first().isPresent()) {
                EnumComposition runePouch = client.getEnum(982);
                if (runePouch.getIntValue(client.getVarbitValue(29)) == runeId && client.getVarbitValue(1624) >= amount) {
                    return true;
                }

                if (runePouch.getIntValue(client.getVarbitValue(1622)) == runeId && client.getVarbitValue(1625) >= amount) {
                    return true;
                }

                if (runePouch.getIntValue(client.getVarbitValue(1623)) == runeId && client.getVarbitValue(1626) >= amount) {
                    return true;
                }

                if (runePouch.getIntValue(client.getVarbitValue(14285)) == runeId && client.getVarbitValue(14286) >= amount) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean hasRune(int runeId) {
        if (inventoryContains(runeId)) {
            return true;
        } else {
            if (Inventory.search().matchesWildCardNoCase("rune pouch").first().isPresent()) {
                EnumComposition runePouch = client.getEnum(982);
                if (runePouch.getIntValue(client.getVarbitValue(29)) == runeId) {
                    return true;
                }

                if (runePouch.getIntValue(client.getVarbitValue(1622)) == runeId) {
                    return true;
                }

                if (runePouch.getIntValue(client.getVarbitValue(1623)) == runeId) {
                    return true;
                }

                if (runePouch.getIntValue(client.getVarbitValue(14285)) == runeId) {
                    return true;
                }
            }

            return false;
        }
    }

}
