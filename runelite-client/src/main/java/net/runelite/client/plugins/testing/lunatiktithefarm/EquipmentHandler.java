package net.runelite.client.plugins.testing.lunatiktithefarm;

import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.ethanapi.collections.Inventory;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class EquipmentHandler
{
    private final AutoTitheFarmPlugin plugin;

    private final AutoTitheFarmConfig config;

    @Setter(AccessLevel.PACKAGE)
    private String gearName;

    private final String action;

    @Inject
    private EquipmentHandler(AutoTitheFarmPlugin plugin, AutoTitheFarmConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.action = "Wear";
    }

    private List<Widget> getGear() {
        return Inventory.search().nameContains(this.gearName).result();
    }

    public boolean isInInventory() {
        return config.switchGearDuringHarvestingPhase() && !getGear().isEmpty();
    }

    public void gearSwitch() {
        if (plugin.actionDelayHandler.isWaitForAction()) {
            return;
        }
        getGear().forEach(itm -> {
            ClickWidget.widgetAction(itm, this.action);
            log.info("Equipping {}", Text.removeTags(itm.getName()));
        });
    }
}
