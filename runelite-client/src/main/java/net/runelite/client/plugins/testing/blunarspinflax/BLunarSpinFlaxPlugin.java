package net.runelite.client.plugins.testing.blunarspinflax;

import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.BankUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.InventoryUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.SpellUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.SpellUtility.SpellBook;
import net.runelite.client.plugins.testing.balaclavaapi.utility.DoAction;
import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import net.runelite.client.plugins.testing.ethanapi.collections.Bank;
import net.runelite.client.plugins.testing.ethanapi.collections.BankInventory;
import net.runelite.client.plugins.testing.ethanapi.collections.Equipment;
import net.runelite.client.plugins.testing.ethanapi.collections.WidgetInfoExtended;
import net.runelite.client.plugins.testing.ethanapi.collections.Widgets;
import java.awt.Color;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
        name = ".B~Lunar Spin Flax",
        description = "Cast spin flax at a bank",
        tags = {"Spin Flax", "Balaclava"},
        enabledByDefault = false
)

public class BLunarSpinFlaxPlugin extends Plugin
{
    private static final Logger log = LoggerFactory.getLogger(BLunarSpinFlaxPlugin.class);
    private final Client client = (Client)RuneLite.getInjector().getInstance(Client.class);
    List AIR_STAFF = List.of(1397, 1405, 1381, 11998, 12000, 20736, 20739, 20730, 20733);
    int alchTickTimer = 0;

    protected void startUp() throws Exception {
        if (this.client.getGameState().equals(GameState.LOGGED_IN)) {
            this.client.getWidget(10616865).setTextColor(Color.GREEN.getRGB());
        }

        EthanApi.init();
        DoAction.setPrintMenuActions(true);
    }

    protected void shutDown() throws Exception {
        if (this.client.getGameState().equals(GameState.LOGGED_IN)) {
            this.client.getWidget(10616865).setTextColor(Color.WHITE.getRGB());
        }

    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.client.getBoostedSkillLevel(Skill.MAGIC) < 76) {
            EthanApi.sendClientMessage("Magic level is too low for Spin flax");
            this.endPlugin();
        } else if (InventoryUtility.inventoryContains(new int[]{1779}) && this.canCastSpinFlax()) {
            if (BankUtility.bankIsOpen()) {
                this.alchTickTimer = 1;
                BankUtility.closeBank();
            } else if (this.alchTickTimer == 0) {
                SpellUtility.cast(WidgetInfoExtended.SPELL_SPIN_FLAX);
            }

            if (this.alchTickTimer == 0) {
                this.alchTickTimer = 4;
            } else {
                --this.alchTickTimer;
            }

        } else if (!BankUtility.bankIsOpen()) {
            if (BankUtility.bankIsPresent() && !EthanApi.isMoving()) {
                BankUtility.openBank();
            }

        } else if (InventoryUtility.inventoryContains(new int[]{1777})) {
            BankInventory.search().withId(1777).first().ifPresent(BankUtility::depositAll);
        } else if (!InventoryUtility.hasRune(9075, 1)) {
            Bank.search().withId(9075).quantityGreaterThan(1).first().ifPresentOrElse(BankUtility::withdrawAll, () -> {
                EthanApi.sendClientMessage("You do not have enough astral runes");
                EthanApi.stopPlugin(this);
            });
        } else if (!InventoryUtility.hasRune(561, 2) && Equipment.search().withId(22370).first().isEmpty()) {
            Bank.search().withId(561).first().ifPresentOrElse(BankUtility::withdrawAll, () -> {
                EthanApi.sendClientMessage("You do not have enough nature runes");
                EthanApi.stopPlugin(this);
            });
        } else if (Equipment.search().idInList(this.AIR_STAFF).first().isEmpty() && !InventoryUtility.hasRune(556, 5) && !InventoryUtility.hasRune(4697, 5) && !InventoryUtility.hasRune(4695, 5) && !InventoryUtility.hasRune(4696, 5)) {
            Bank.search().withId(556).first().ifPresentOrElse(BankUtility::withdrawAll, () -> {
                EthanApi.sendClientMessage("You do not have enough air runes");
                EthanApi.stopPlugin(this);
            });
        } else if (!InventoryUtility.inventoryContains(new int[]{1779})) {
            Bank.search().withId(1779).first().ifPresentOrElse(BankUtility::withdrawAll, () -> {
                EthanApi.sendClientMessage("You do not have any flax");
                EthanApi.stopPlugin(this);
            });
        }
    }

    private void endPlugin() {
        try {
            EthanApi.stopPlugin(this);
            if (Widgets.search().withId(10616865).first().isPresent()) {
                this.client.getWidget(10616865).setTextColor(Color.WHITE.getRGB());
            }

        } catch (Throwable var2) {
            throw var2;
        }
    }

    private boolean canCastSpinFlax() {
        return SpellUtility.getCurrentSpellBook().equals(SpellBook.LUNAR) && InventoryUtility.hasRune(561, 2) && InventoryUtility.hasRune(9075, 1) && (InventoryUtility.hasRune(556, 5) || InventoryUtility.hasRune(4697, 5) || InventoryUtility.hasRune(4695, 5) || InventoryUtility.hasRune(4696, 5));
    }

}
