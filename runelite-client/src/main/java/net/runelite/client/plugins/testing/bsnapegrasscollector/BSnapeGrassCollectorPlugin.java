package net.runelite.client.plugins.testing.bsnapegrasscollector;

import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.BankUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.InventoryUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.PlayerUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.SpellUtility;
import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.SpellUtility.SpellBook;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickObject;
import net.runelite.client.plugins.testing.balaclavaapi.utility.DoAction;
import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import net.runelite.client.plugins.testing.ethanapi.collections.Bank;
import net.runelite.client.plugins.testing.ethanapi.collections.BankInventory;
import net.runelite.client.plugins.testing.ethanapi.collections.TileItems;
import net.runelite.client.plugins.testing.ethanapi.collections.WidgetInfoExtended;
import net.runelite.client.plugins.testing.ethanapi.collections.Widgets;
import java.awt.Color;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
        name = ".B~Snape Grass Collector",
        description = "Collects snape grass from waterbirth, and teleports moonclan teleports to the bank",
        tags = {"snape", "grass", "Balaclava"},
        enabledByDefault = false
)
public class BSnapeGrassCollectorPlugin extends Plugin
{
    private static final Logger log = LoggerFactory.getLogger(BSnapeGrassCollectorPlugin.class);
    private final Client client = (Client)RuneLite.getInjector().getInstance(Client.class);
    private final int REGION_LUNAR_ISLE = 8253;
    private final int REGION_LUNAR_ISLE2 = 8509;
    private final int REGION_WATERBIRTH = 10042;

    protected void startUp() throws Exception {
        if (this.client.getGameState().equals(GameState.LOGGED_IN)) {
            this.client.getWidget(10616865).setTextColor(Color.GREEN.getRGB());
        }

        DoAction.setPrintMenuActions(true);
        EthanApi.init();
    }

    protected void shutDown() throws Exception {
        if (this.client.getGameState().equals(GameState.LOGGED_IN)) {
            this.client.getWidget(10616865).setTextColor(Color.WHITE.getRGB());
        }

    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!SpellUtility.getCurrentSpellBook().equals(SpellBook.LUNAR)) {
            EthanApi.sendClientMessage("You need to be on the lunar spellbook to use this plugin");
            EthanApi.stopPlugin(this);
        }

        if (PlayerUtility.getRunEnergy() > 20 && !PlayerUtility.runEnabled()) {
            PlayerUtility.toggleRunEnergy();
        }

        if (PlayerUtility.inRegion(10042)) {
            if (InventoryUtility.inventoryIsFull()) {
                if (PlayerUtility.isIdle()) {
                    if (this.canCastMoonclanTeleport()) {
                        SpellUtility.cast(WidgetInfoExtended.SPELL_MOONCLAN_TELEPORT);
                    } else {
                        EthanApi.sendClientMessage("Cannot cast Moonclan teleport");
                        EthanApi.stopPlugin(this);
                    }
                }

            } else {
                TileItems.search().withId(231).withinDistanceToPoint(18, new WorldPoint(2553, 3751, 0)).nearestByPath().ifPresent((snapeGrass) -> {
                    if (!EthanApi.isMoving()) {
                        ClickObject.lootTileItem(snapeGrass);
                    }

                });
            }
        } else if (!PlayerUtility.inRegion(8253) && !PlayerUtility.inRegion(8509)) {
            if (this.canCastMoonclanTeleport()) {
                SpellUtility.cast(WidgetInfoExtended.SPELL_MOONCLAN_TELEPORT);
            } else {
                EthanApi.sendClientMessage("Cannot cast Moonclan teleport");
                EthanApi.stopPlugin(this);
            }

        } else if (!InventoryUtility.inventoryIsFull() && !InventoryUtility.inventoryContains(new int[]{231}) && this.canCastMoonclanTeleport() && this.canCastWaterbirthTeleport()) {
            if (PlayerUtility.isIdle()) {
                if (this.canCastWaterbirthTeleport()) {
                    SpellUtility.cast(WidgetInfoExtended.SPELL_WATERBIRTH_TELEPORT);
                } else {
                    EthanApi.sendClientMessage("Cannot cast Waterbirth teleport");
                    EthanApi.stopPlugin(this);
                }
            }

        } else {
            if (!BankUtility.bankIsOpen()) {
                if (!EthanApi.isMoving()) {
                    BankUtility.openBank();
                }
            } else {
                if (!InventoryUtility.hasRune(9075, 2)) {
                    Bank.search().withId(9075).quantityGreaterThan(1).first().ifPresentOrElse(BankUtility::withdrawAll, () -> {
                        EthanApi.sendClientMessage("You do not have enough astral runes");
                        EthanApi.stopPlugin(this);
                    });
                    return;
                }

                if (!InventoryUtility.hasRune(563, 1)) {
                    Bank.search().withId(563).first().ifPresentOrElse(BankUtility::withdrawAll, () -> {
                        EthanApi.sendClientMessage("You do not have enough law runes");
                        EthanApi.stopPlugin(this);
                    });
                    return;
                }

                if (!InventoryUtility.hasRune(555, 1) && !InventoryUtility.hasRune(4490, 1) && !InventoryUtility.hasRune(4695, 1) && !InventoryUtility.hasRune(4694, 1)) {
                    Bank.search().withId(555).first().ifPresentOrElse(BankUtility::withdrawAll, () -> {
                        EthanApi.sendClientMessage("You do not have enough water runes");
                        EthanApi.stopPlugin(this);
                    });
                    return;
                }

                if (!InventoryUtility.hasRune(557, 1) && !InventoryUtility.hasRune(4490, 1) && !InventoryUtility.hasRune(4696, 1) && !InventoryUtility.hasRune(4699, 1)) {
                    Bank.search().withId(557).first().ifPresentOrElse(BankUtility::withdrawAll, () -> {
                        EthanApi.sendClientMessage("You do not have enough earth runes");
                        EthanApi.stopPlugin(this);
                    });
                    return;
                }

                BankInventory.search().withId(231).first().ifPresent(BankUtility::depositAll);
            }

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

    private boolean canCastMoonclanTeleport() {
        return SpellUtility.getCurrentSpellBook().equals(SpellBook.LUNAR) && InventoryUtility.hasRune(563, 1) && InventoryUtility.hasRune(9075, 2) && (InventoryUtility.hasRune(557, 1) || InventoryUtility.hasRune(4698, 1) || InventoryUtility.hasRune(4696, 1) || InventoryUtility.hasRune(4699, 1));
    }

    private boolean canCastWaterbirthTeleport() {
        return SpellUtility.getCurrentSpellBook().equals(SpellBook.LUNAR) && InventoryUtility.hasRune(563, 1) && InventoryUtility.hasRune(9075, 2) && (InventoryUtility.hasRune(555, 1) || InventoryUtility.hasRune(4698, 1) || InventoryUtility.hasRune(4694, 1) || InventoryUtility.hasRune(4695, 1));
    }

}
