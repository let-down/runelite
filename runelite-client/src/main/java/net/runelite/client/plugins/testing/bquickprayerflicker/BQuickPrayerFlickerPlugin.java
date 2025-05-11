package net.runelite.client.plugins.testing.bquickprayerflicker;

import net.runelite.client.plugins.testing.balaclavaapi.interactionutility.PrayerUtility;
import net.runelite.client.plugins.testing.balaclavaapi.utility.DoAction;
import net.runelite.client.plugins.testing.ethanapi.collections.Widgets;
import net.runelite.client.plugins.testing.ethanapi.EthanApi;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


import java.awt.*;

@PluginDescriptor(
        name = ".B~Quick Prayer Flicker",
        description = "One tick flicks quick prayers",
        tags = {"prayer","flicker","Balaclava"},
        enabledByDefault = false
)
@Slf4j
public class BQuickPrayerFlickerPlugin extends Plugin
{
    private final Client client = RuneLite.getInjector().getInstance(Client.class);

    @Override
    protected void startUp() throws Exception {
        if(client.getGameState().equals(GameState.LOGGED_IN)) {
            //change login timer to green when its on
            client.getWidget(10616865).setTextColor(Color.GREEN.getRGB());
        }
        DoAction.setPrintMenuActions(true);
        EthanApi.init();
    }
    @Override
    protected void shutDown() throws Exception {
        if(client.getGameState().equals(GameState.LOGGED_IN)) {
            //back to white when its off
            client.getWidget(10616865).setTextColor(Color.WHITE.getRGB());
        }
    }



    @Subscribe
    public void onGameTick(GameTick event) {

        if(client.getBoostedSkillLevel(Skill.PRAYER) <= 0 ){
            endPlugin();
        }

        if(PrayerUtility.isQuickPrayerEnabled()){
            PrayerUtility.toggleQuickPrayer();
        }
        PrayerUtility.toggleQuickPrayer();


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
