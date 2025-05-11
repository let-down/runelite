package net.runelite.client.plugins.testing.bcooker;

import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.BankUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.InventoryUtility;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickNpc;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickObject;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.balaclavaapi.utility.DoAction;
import net.runelite.client.plugins.testing.ethanapi.collections.*;
import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

import java.awt.*;
import java.util.Optional;

@PluginDescriptor(
        name = ".B~Cooker",
        description = "Cook food wherever there is a bank and a cooking object",
        tags = {"Cooker","Rogue", "Balaclava"},
        enabledByDefault = false
)
@Slf4j
public class BCookerPlugin extends Plugin
{
    private Client client = RuneLite.getInjector().getInstance(Client.class);
    private KeyManager keyManager = RuneLite.getInjector().getInstance(KeyManager.class);
    @Inject
    private BCookerConfig config;

    @Provides
    private BCookerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(BCookerConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if(client.getGameState().equals(GameState.LOGGED_IN)) {
            //change login timer to green when its on
            client.getWidget(10616865).setTextColor(Color.GREEN.getRGB());
        }
        EthanApi.init();
        DoAction.setPrintMenuActions(true);
    }
    @Override
    protected void shutDown() throws Exception {
        if(client.getGameState().equals(GameState.LOGGED_IN)) {
            //back to white when its off
            client.getWidget(10616865).setTextColor(Color.WHITE.getRGB());
        }
    }

    int cookingTimeout = 6;

    @Subscribe
    public void onGameTick(GameTick event) {
        if(continueClick()){cookingTimeout = 0;return;}

        if(Widgets.search().withTextContains("How many would you like to cook").first().isPresent()){
            Widgets.search().withAction("Cook").withId(17694734).first().ifPresent(cook -> {
                setAmount();
                ClickWidget.widgetAction(cook,"Cook");
            });
            return;
        }

        if(Widgets.search().withTextContains("What would you like to cook").first().isPresent()){
            Widgets.search().withAction("Cook").withId(17694734).first().ifPresent(cook -> {
                setAmount();
                ClickWidget.widgetAction(cook,"Cook");
            });
            return;
        }


        //we have food to cook
        if(Inventory.search().matchesWildCardNoCase(config.rawItem()).first().isPresent()){
            if(client.getLocalPlayer().getAnimation() == -1){
                cookingTimeout --;
            }

            //start cooking
            if(cookingTimeout <= 0){
                TileObjects.search().withAction("Cook").nearestByPath().ifPresent(cookingObject ->{
                    if(!EthanApi.isMoving()) {
                        ClickObject.objectAction(cookingObject, "Cook");
                    }
                });
            }

        } else {
            //we are out of food so set timeout to 0 so we can begin immediately after banking
            cookingTimeout = 0;
            //closes the bugged banking interface that is caused with this API
            client.runScript(ScriptID.MESSAGE_LAYER_CLOSE, 1, 1, 0);
            //get a banking npc or object
            if(!BankUtility.bankIsOpen()) {
                BankUtility.openBank();
            } else {
                if(Inventory.getEmptySlots() != 28) {
                    Widgets.search().withId(786474).first().ifPresent(depositAll -> {
                        ClickWidget.widgetAction(depositAll, "Deposit inventory");
                    });
                } else {
                    if(Bank.search().matchesWildCardNoCase(config.rawItem()).quantityGreaterThan(0).first().isPresent()){
                        BankUtility.withdrawX(Bank.search().matchesWildCardNoCase(config.rawItem()).first().get(),28);
                    } else {
                        endPlugin();
                    }
                }
            }
        }
    }


    private void endPlugin(){
        EthanApi.stopPlugin(this);
        EthanApi.sendClientMessage("Out of " + config.rawItem());
        client.getWidget(WidgetInfo.CHATBOX_REPORT_TEXT).setTextColor(Color.WHITE.getRGB());
    }

    private boolean continueClick(){
        Optional<Widget> mainContinueOpt = Widgets.search().withTextContains("Click here to continue").first();
        if (mainContinueOpt.isPresent()) {
            ClickWidget.clickWidget(mainContinueOpt.get(),MenuAction.WIDGET_CONTINUE, -1);
            return true;
        }

        Optional<Widget> continue1Opt = Widgets.search().withId(12648448).hiddenState(false).first();
        if (continue1Opt.isPresent()) {
            ClickWidget.clickWidget(continue1Opt.get(),MenuAction.WIDGET_CONTINUE, -1);
            return true;
        }
        Optional<Widget> continue2Opt = Widgets.search().withId(41484288).hiddenState(false).first();
        if (continue2Opt.isPresent()) {
            ClickWidget.clickWidget(continue2Opt.get(),MenuAction.WIDGET_CONTINUE, -1);
            return true;
        }

        return false;
    }


    @Subscribe public void onAnimationChanged(AnimationChanged e){
        if(e.getActor().equals(client.getLocalPlayer())){
            if(e.getActor().getAnimation() == 897 || e.getActor().getAnimation() == 896){
                cookingTimeout = 6;
            }
        }
    }

    private void setAmount(){
        Widgets.search().nameMatchesWildCardNoCase(config.rawItem()).first().ifPresent(item->{
            if(client.getVarcIntValue(200) < InventoryUtility.inventoryAmount(item.getItemId())){
                client.setVarcIntValue(200,InventoryUtility.inventoryAmount(item.getItemId()));
            }
        });
    }
}
