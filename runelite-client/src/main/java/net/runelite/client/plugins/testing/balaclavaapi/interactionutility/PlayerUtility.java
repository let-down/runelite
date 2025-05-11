package net.runelite.client.plugins.testing.balaclavaapi.interactionutility;

import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.ethanapi.collections.Widgets;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;


public class PlayerUtility
{
    static Client client = (Client)RuneLite.getInjector().getInstance(Client.class);

    public static boolean inRegion(int regionID) {
        return WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID() == regionID;
    }

    public static boolean isIdle() {
        return client.getLocalPlayer().getAnimation() == -1;
    }

    public static boolean runEnabled() {
        return client.getVarpValue(173) == 1;
    }

    public static int getRunEnergy() {
        return client.getEnergy() / 100;
    }

    public static void toggleRunEnergy() {
        if (getRunEnergy() > 0) {
            Widgets.search().withAction("Toggle run").first().ifPresent((toggleRun) -> {
                ClickWidget.widgetAction(toggleRun, new String[]{"Toggle run"});
            });
        }
    }

}
