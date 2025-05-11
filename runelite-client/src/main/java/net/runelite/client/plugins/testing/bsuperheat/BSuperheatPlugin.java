package net.runelite.client.plugins.testing.bsuperheat;

import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.BankUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.SpellUtility;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.ethanapi.collections.*;
import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@PluginDescriptor(
        name = ".B~Superheat",
        description = "Cast superheat on selected ores",
        tags = {"superheat","Balaclava"},
        enabledByDefault = false
)
@Slf4j
public class BSuperheatPlugin extends Plugin
{
    private final Client client = RuneLite.getInjector().getInstance(Client.class);
    private final ClientThread clientThread = RuneLite.getInjector().getInstance(ClientThread.class);
    private final ItemManager itemManager = RuneLite.getInjector().getInstance(ItemManager.class);
    @Inject
    private BSuperheatConfig config;

    @Provides
    private BSuperheatConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(BSuperheatConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if (client.getGameState().equals(GameState.LOGGED_IN)) {
            //change login timer to green when its on
            client.getWidget(10616865).setTextColor(Color.GREEN.getRGB());
        }

        EthanApi.init();
    }

    @Override
    protected void shutDown() throws Exception {
        if (client.getGameState().equals(GameState.LOGGED_IN)) {
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

    int tickTimer = 0;


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();

    @Subscribe
    public void onGameTick(GameTick event) {

        Runnable task = () -> {
            clientThread.invoke(this::runScript);
        };

        // Schedule the task to run once after a delay of 5 seconds
        scheduler.schedule(task, random.nextInt(15), TimeUnit.MILLISECONDS);






    }

    public void runScript(){
        if(!hasFireRunes()){
            if(BankUtility.bankIsPresent()){
                if (BankUtility.bankIsPresent()) {
                    if (!BankUtility.bankIsOpen()) {
                        BankUtility.openBank();
                        return;
                    } else {
                        Optional<Widget> fireRune = Bank.search().withId(ItemID.FIRE_RUNE).first();
                        fireRune.ifPresentOrElse(BankUtility::withdrawAll, () -> {
                            EthanApi.sendClientMessage("Did not find any fire runes");
                            endPlugin();
                        });
                        return;
                    }
                }
            }
            return;
        }

        if(!hasNatureRunes()){
            if(BankUtility.bankIsPresent()){
                if (BankUtility.bankIsPresent()) {
                    if (!BankUtility.bankIsOpen()) {
                        BankUtility.openBank();
                        return;
                    } else {
                        Optional<Widget> fireRune = Bank.search().withId(ItemID.NATURE_RUNE).first();
                        fireRune.ifPresentOrElse(BankUtility::withdrawAll, () -> {
                            EthanApi.sendClientMessage("Did not find any nature runes");
                            endPlugin();
                        });
                        return;
                    }
                }
            }
            return;
        }

        if(client.getBoostedSkillLevel(Skill.MAGIC) < 55){
            EthanApi.sendClientMessage("Magic level is too low for High level alchemy");
            endPlugin();
            return;
        }


        boolean hasOres = true;

        for(ItemWithAmount itemWithAmount :config.bar().getItems()){
            if(Inventory.search().withId(itemWithAmount.getItemId()).result().size() < itemWithAmount.getAmount() ){
                hasOres = false;
                break;
            }
        }

        if(hasOres){
            if(BankUtility.bankIsOpen()){
                BankUtility.closeBank();
                return;
            }

            if(tickTimer == 0) {
                Inventory.search().withId(config.bar().getItems()[0].getItemId()).first().ifPresent(item -> {
                    SpellUtility.castOnInventoryItem(WidgetInfoExtended.SPELL_SUPERHEAT_ITEM,item.getItemId());
                });
            }
        } else {
            //bank
            if(!BankUtility.bankIsOpen()) {
                if(BankUtility.bankIsPresent()){
                    BankUtility.openBank();
                }
            } else {
                if(Inventory.getItemAmount(config.bar().getProduct()) > 0){
                    BankUtility.depositAll(config.bar().getProduct());
                    return;
                } else {

                    int openSlots = Inventory.getEmptySlots();
                    int totalBarsPossible = openSlots/config.bar().getTotalItems();


                    for(ItemWithAmount itemWithAmount : config.bar().getItems()){
                        //withdraw an appropriate amount for inventory size
                        if(Bank.search().withId(itemWithAmount.getItemId()).quantityGreaterThan(itemWithAmount.getAmount()*totalBarsPossible).first().isPresent()){
                            BankUtility.withdrawX(Bank.search().withId(itemWithAmount.getItemId()).first().get(),(itemWithAmount.getAmount()*totalBarsPossible) - Inventory.search().withId(itemWithAmount.getItemId()).result().size());
                        } else if(Bank.search().withId(itemWithAmount.getItemId()).quantityGreaterThan(itemWithAmount.getAmount() - 1).first().isPresent()){
                            //if theres not enough to fill inventory, just get all
                            Bank.search().withId(itemWithAmount.getItemId()).first().ifPresent(ore -> {
                                ClickWidget.widgetAction(ore,"withdraw-all");
                            });
                        } else {
                            EthanApi.sendClientMessage("You have run out of " + itemManager.getItemComposition(itemWithAmount.getItemId()).getName());
                            endPlugin();
                        }


                    }
                    client.runScript(ScriptID.MESSAGE_LAYER_CLOSE, 1, 1, 0);


                }
            }
        }

        if(tickTimer <= 0){
            tickTimer = 2;
        } else {
            tickTimer--;
        }
    }


    private boolean hasFireRunes(){
        if(Equipment.search().idInList(FIRE_STAFF).first().isPresent()){
            return true;
        }

        for(int runeId : FIRE_RUNE){
            if(Inventory.search().withId(runeId).quantityGreaterThan(3).first().isPresent()){
                return true;
            }
        }


        if(Inventory.search().matchesWildCardNoCase("rune pouch").first().isPresent()){
            EnumComposition runePouch = client.getEnum(EnumID.RUNEPOUCH_RUNE);
            for(int runeId : FIRE_RUNE){
                if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE1)) == runeId){
                    if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT1) > 3){
                        return true;
                    }
                }
                if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE2)) == runeId){
                    if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT2) > 3){
                        return true;
                    }
                }
                if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE3)) == runeId){
                    if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT3) > 3){
                        return true;
                    }
                }
                if(runePouch.getIntValue(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE4)) == runeId){
                    if(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT4) > 3){
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
