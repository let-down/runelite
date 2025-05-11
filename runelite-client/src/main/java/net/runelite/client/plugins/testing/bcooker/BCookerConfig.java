package net.runelite.client.plugins.testing.bcooker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("Bcooker")
public interface BCookerConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "rawItem",
            name = "Raw Item",
            description = "Raw item to get from the bank"
    )
    default String rawItem() {
        return "Raw swordfish";
    }
}
