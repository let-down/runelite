package net.runelite.client.plugins.testing.balaclavaapi.interactionutility;

import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickNpc;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickObject;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.ethanapi.collections.*;
import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.ScriptID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;

import java.util.Optional;

public class BankUtility {

    static Client client = RuneLite.getInjector().getInstance(Client.class);

    static final int WITHDRAW_QUANTITY = 3960;
    static final int BANK_CONTAINER_WIDGET = 786433;

    public static boolean bankContains(int itemId){
        return Bank.search().withId(itemId).first().isPresent();
    }

    public static boolean bankContains(int itemId, int amount){
        Optional<Widget> bankItem = Bank.search().withId(itemId).first();
        return bankItem.isPresent() && bankItem.get().getItemQuantity() >= amount;
    }

    public static void openBank(){
        if(bankIsOpen()){
            return;
        }

        if (NPCs.search().withAction("Bank").walkable().first().isPresent()) {
            NPCs.search().withAction("Bank").walkable().first().ifPresent(bankNpc -> {
                if(!EthanApi.isMoving()) {
                    ClickNpc.npcAction(bankNpc, "Bank");
                }
            });
        } else if (TileObjects.search().nameContains("booth").withAction("Bank").first().isPresent()){
            TileObjects.search().nameContains("booth").withAction("Bank").nearestByPath().ifPresent(bankObject ->{
                if(!EthanApi.isMoving()) {
                    ClickObject.objectAction(bankObject, "Bank");
                }
            });
        } else if (TileObjects.search().nameContains("Bank").withAction("Use").first().isPresent()){
            TileObjects.search().nameContains("Bank").withAction("Use").nearestByPath().ifPresent(bankObject ->{
                if(!EthanApi.isMoving()) {
                    ClickObject.objectAction(bankObject, "Use","Bank");
                }
            });
        } else {
            TileObjects.search().nameContains("Bank").withAction("Bank").nearestByPath().ifPresent(bankObject ->{
                if(!EthanApi.isMoving()) {
                    ClickObject.objectAction(bankObject, "Use","Bank");
                }
            });
        }
    }
    public static void closeBank(){
        if(!bankIsOpen()){
            return;
        }
        Optional<Widget> depositWidget = Widgets.search().withId(786434).first();
        depositWidget.ifPresent(widget -> ClickWidget.clickWidget(11,widget, MenuAction.CC_OP,1));
        client.runScript(ScriptID.MESSAGE_LAYER_CLOSE, 1, 1, 0);


    }

    public static boolean bankIsPresent(){
        // Check for a walkable NPC with "Bank" action
        if (NPCs.search().withAction("Bank").walkable().first().isPresent()) {
            return true;
        }

        // Check for a booth tile object with "Bank" action
        if (TileObjects.search().nameContains("booth").withAction("Bank").first().isPresent()) {
            return true;
        }

        // Check for a "Bank" object with "Use" action
        if (TileObjects.search().nameContains("Bank").withAction("Use").first().isPresent()) {
            return true;
        }

        // Check for a "Bank" object with "Bank" action
        if (TileObjects.search().nameContains("Bank").withAction("Bank").first().isPresent()) {
            return true;
        }
        return false;
    }
    public static boolean bankIsOpen(){
        Optional<Widget> b = Widgets.search().withId(BANK_CONTAINER_WIDGET).first();
        return b.isPresent() && !b.get().isHidden();
    }
    public static void depositAll(int itemId){
        Optional<Widget> item = BankInventory.search().withId(itemId).first();
        item.ifPresent(BankUtility::depositAll);
    }
    public static void depositAll(Widget widget){
        ClickWidget.widgetAction(widget,"Deposit-all");
    }
    public static void depositAll(){
        Optional<Widget> depositWidget = Widgets.search().withAction("Deposit inventory").first();
        depositWidget.ifPresent(widget -> ClickWidget.widgetAction(widget, "Deposit inventory"));
    }

    public static void withdrawAll(Widget widget){
        if(widget == null){
            return;
        }

        ClickWidget.widgetAction(widget,"Withdraw-all");
    }

    public static void withdrawX(int itemId, int amount){
        Optional<Widget> item = Bank.search().withId(itemId).first();
        item.ifPresent(i-> BankUtility.withdrawX(item.get(),amount));
    }
    public static void withdrawX(Widget widget, int amount){
        if (client.getVarbitValue(WITHDRAW_QUANTITY) == amount) {
           ClickWidget.widgetAction(widget, "Withdraw-" + client.getVarbitValue(WITHDRAW_QUANTITY));
           return;
        }

        ClickWidget.widgetAction(widget, "Withdraw-X");
        client.setVarcStrValue(359, Integer.toString(amount));
        client.setVarcIntValue(5, 7);
        client.runScript(681);
        client.setVarbit(WITHDRAW_QUANTITY, amount);
        client.runScript(ScriptID.MESSAGE_LAYER_CLOSE, 1, 1, 0);

    }

    public static void useInBank(int itemId){
        Optional<Widget> bankItem = BankInventory.search().withId(itemId).first();

        if(bankItem.isEmpty()){
            return;
        }

        ClickWidget.widgetAction(bankItem.get(),"wield","wear","eat","drink");
    }

    public static void useInBank(Widget item){
        if(item == null){
            return;
        }
        ClickWidget.widgetAction(item,"wield","wear","eat","drink");
    }


}
