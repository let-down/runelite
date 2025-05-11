package net.runelite.client.plugins.testing.bhighalch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Bhighalch")
public interface BHighAlchConfig extends Config
{
    @ConfigItem(
            keyName = "items",
            name = "Items",
            description = "Items to cast high level alchemy on, separated by commas"
    )
    default String items() {
        return "rune platebody, rune kiteshield";
    }
}
