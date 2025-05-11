package net.runelite.client.plugins.testing.bsuperheat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Bsuperheat")
public interface BSuperheatConfig extends Config
{
    @ConfigItem(
            keyName = "bar",
            name = "Bar",
            description = "Bar to create with superheat"
    )
    default Bar bar() {
        return Bar.GOLD_BAR;
    }
}
