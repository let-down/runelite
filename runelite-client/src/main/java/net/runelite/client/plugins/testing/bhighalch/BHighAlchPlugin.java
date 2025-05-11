package net.runelite.client.plugins.testing.bhighalch;

import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.SpellUtility;
import net.runelite.client.plugins.testing.ethanapi.collections.Equipment;
import net.runelite.client.plugins.testing.ethanapi.collections.Inventory;
import net.runelite.client.plugins.testing.ethanapi.collections.WidgetInfoExtended;
import net.runelite.client.plugins.testing.ethanapi.collections.Widgets;
import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PluginDescriptor(
        name = ".B~High Alchemy",
        description = "Cast High Alchemy on selected items",
        tags = {"Alchemy","Balaclava"},
        enabledByDefault = false
)
@Slf4j
public class BHighAlchPlugin extends Plugin
{
    private final Client client = RuneLite.getInjector().getInstance(Client.class);
    @Inject
    private BHighAlchConfig config;

    @Provides
    private BHighAlchConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(BHighAlchConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if(client.getGameState().equals(GameState.LOGGED_IN)) {
            //change login timer to green when its on
            client.getWidget(10616865).setTextColor(Color.GREEN.getRGB());
        }
        EthanApi.init();
    }
    @Override
    protected void shutDown() throws Exception {
        if(client.getGameState().equals(GameState.LOGGED_IN)) {
            //back to white when its off
            client.getWidget(10616865).setTextColor(Color.WHITE.getRGB());
        }
    }


    List<Integer> FIRE_STAFF = List.of(
            ItemID.FIRE_BATTLESTAFF,
            ItemID.MYSTIC_FIRE_STAFF,
            ItemID.STAFF_OF_FIRE,
            ItemID.SMOKE_BATTLESTAFF,
            ItemID.MYSTIC_SMOKE_STAFF,
            ItemID.LAVA_BATTLESTAFF,
            ItemID.LAVA_BATTLESTAFF_21198,
            ItemID.MYSTIC_LAVA_STAFF,
            ItemID.MYSTIC_LAVA_STAFF_21200,
            ItemID.STEAM_BATTLESTAFF,
            ItemID.STEAM_BATTLESTAFF_12795,
            ItemID.MYSTIC_STEAM_STAFF,
            ItemID.MYSTIC_STEAM_STAFF_12796,
            ItemID.TOME_OF_FIRE
    );

    List<Integer> FIRE_RUNE = List.of(
            ItemID.FIRE_RUNE,
            ItemID.SMOKE_RUNE,
            ItemID.LAVA_RUNE,
            ItemID.STEAM_RUNE
    );


    int alchTickTimer = 0;
    @Subscribe
    public void onGameTick(GameTick event) {

        if(!hasFireRunes()){
            EthanApi.sendClientMessage("Did not find any fire runes");
            endPlugin();
            return;
        }

        if(!hasNatureRunes()){
            EthanApi.sendClientMessage("Did not find any nature runes");
            endPlugin();
            return;
        }

        if(client.getBoostedSkillLevel(Skill.MAGIC) < 55){
            EthanApi.sendClientMessage("Magic level is too low for High level alchemy");
            endPlugin();
            return;
        }


        if (alchTickTimer == 0){
            Widget highAlchSpellWidget = Widgets.search().withParentId(14286851).nameMatchesWildCardNoCase("high level alchemy").first().get();

            List<String> itemsList = Stream.of(config.items().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            if(Inventory.search().nameInList(itemsList).first().isPresent()){
                Inventory.search().nameInList(itemsList).first().ifPresent(item -> {
                    SpellUtility.castOnInventoryItem(WidgetInfoExtended.SPELL_HIGH_LEVEL_ALCHEMY,item.getItemId());
                });
            } else {
                EthanApi.sendClientMessage("Did not find any alchables");
                endPlugin();
            }
        }

        if(alchTickTimer == 0){
            alchTickTimer = 4;
        } else {
            alchTickTimer--;
        }

    }


    private boolean hasFireRunes(){
        if(Equipment.search().idInList(FIRE_STAFF).first().isPresent()){
            return true;
        }

        for(int runeId : FIRE_RUNE){
            if(Inventory.search().withId(runeId).quantityGreaterThan(4).first().isPresent()){
                return true;
            }
        }


        if(Inventory.search().matchesWildCardNoCase("rune pouch").first().isPresent()){
            EnumComposition runePouch = client.getEnum(EnumID.RUNEPOUCH_RUNE);
            for(int runeId : FIRE_RUNE){
                if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE1)) == runeId){
                    if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT1) > 4){
                        return true;
                    }
                }
                if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE2)) == runeId){
                    if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT2) > 4){
                        return true;
                    }
                }
                if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE3)) == runeId){
                    if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT3) > 4){
                        return true;
                    }
                }
                if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE4)) == runeId){
                    if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT4) > 4){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasNatureRunes(){
        if(Equipment.search().withId(ItemID.BRYOPHYTAS_STAFF).first().isPresent()){
            return true;
        }

        if(Inventory.search().withId(ItemID.NATURE_RUNE).first().isPresent()){
            return true;
        }

        if(Inventory.search().matchesWildCardNoCase("rune pouch").first().isPresent()){
            EnumComposition runePouch = client.getEnum(EnumID.RUNEPOUCH_RUNE);
            if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE1)) == ItemID.NATURE_RUNE){
                if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT1) >= 1){
                    return true;
                }
            }
            if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE2)) == ItemID.NATURE_RUNE){
                if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT2) >= 1){
                    return true;
                }
            }
            if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE3)) == ItemID.NATURE_RUNE){
                if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT3) >= 1){
                    return true;
                }
            }
            if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE4)) == ItemID.NATURE_RUNE){
                if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT4) >= 1){
                    return true;
                }
            }

        }
        return false;
    }

    @SneakyThrows
    private void endPlugin(){
        //switch plugin off
        EthanApi.stopPlugin(this);

        if(Widgets.search().withId(10616865).first().isPresent()) {
            //change color of the login timer back to white
            client.getWidget(10616865).setTextColor(Color.WHITE.getRGB());
        }

    }
}
