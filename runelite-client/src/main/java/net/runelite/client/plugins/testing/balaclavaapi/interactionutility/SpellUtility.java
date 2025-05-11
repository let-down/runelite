package net.runelite.client.plugins.testing.balaclavaapi.interactionutility;

import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.ethanapi.collections.Inventory;
import net.runelite.client.plugins.testing.ethanapi.collections.WidgetInfoExtended;
import net.runelite.client.plugins.testing.ethanapi.collections.Widgets;
import net.runelite.api.NPC;
import net.runelite.api.widgets.Widget;

import java.util.Optional;

public class SpellUtility {
    static Client client = (Client) RuneLite.getInjector().getInstance(Client.class);

    public static void cast(WidgetInfoExtended spell){
        Optional<Widget> spellWidget = Widgets.search().withParentId(14286851).withId(getSpellId(spell)).first();
        spellWidget.ifPresent(widget -> ClickWidget.widgetAction(widget, "cast"));
    }

    public static void cast(String spellName){
        Optional<Widget> spellWidget = Widgets.search().withParentId(14286851).nameContains(spellName).first();
        spellWidget.ifPresent(widget -> ClickWidget.widgetAction(widget, "cast"));
    }

    public static void castOnNpc(WidgetInfoExtended spell, NPC npc){
        Optional<Widget> spellWidget = Widgets.search().withParentId(14286851).withId(getSpellId(spell)).first();
        spellWidget.ifPresent(widget -> ClickWidget.widgetOnNpc(widget, npc));
    }

    public static void castOnNpc(String spellName, NPC npc){
        if(npc == null){
            return;
        }
        Optional<Widget> spellWidget = Widgets.search().withParentId(14286851).nameContains(spellName).first();
        spellWidget.ifPresent(widget -> ClickWidget.widgetOnNpc(widget, npc));
    }

    public static void castOnWidget(WidgetInfoExtended spell, Widget widget){
        Optional<Widget> spellWidget = Widgets.search().withParentId(14286851).withId(getSpellId(spell)).first();
        spellWidget.ifPresent(toCast -> ClickWidget.widgetOnWidget(toCast,widget));
    }

    public static void castOnInventoryItem(String spellName, int itemId){
        Optional<Widget> spellWidget = Widgets.search().withParentId(14286851).nameContains(spellName).first();
        Optional<Widget> inventoryItem = Inventory.search().withId(itemId).first();

        if(inventoryItem.isEmpty()){
            return;
        }

        spellWidget.ifPresent(toCast -> ClickWidget.widgetOnWidget(toCast,inventoryItem.get()));
    }

    public static void castOnInventoryItem(WidgetInfoExtended spell, int itemId){
        Optional<Widget> spellWidget = Widgets.search().withParentId(14286851).withId(getSpellId(spell)).first();
        Optional<Widget> inventoryItem = Inventory.search().withId(itemId).first();

        if(inventoryItem.isEmpty()){
            return;
        }

        spellWidget.ifPresent(toCast -> ClickWidget.widgetOnWidget(toCast,inventoryItem.get()));
    }



    private static int getSpellId(WidgetInfoExtended spell){

        String input = spell.name();
        String output = input.replace("SPELL_", ""); // Remove "SPELL_" from the input
        output = output.replace("_", " "); // Remove any remaining underscores


        return Widgets.search().withParentId(14286851).nameMatchesWildCardNoCase(output.toLowerCase()).first().get().getId();
    }

    public static SpellBook getCurrentSpellBook() {
        switch (client.getVarbitValue(4070)) {
            case 0:
                return SpellBook.STANDARD;
            case 1:
                return SpellBook.ANCIENT;
            case 2:
                return SpellBook.LUNAR;
            case 3:
                return SpellBook.ARCEUUS;
            default:
                return SpellBook.STANDARD;
        }
    }

    public static enum SpellBook {
        STANDARD,
        LUNAR,
        ARCEUUS,
        ANCIENT;
    }

}
