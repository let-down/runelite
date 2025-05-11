package net.runelite.client.plugins.testing.bbutterflycatcher;

import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickNpc;
import net.runelite.client.plugins.testing.balaclavaapi.utility.ClickWidget;
import net.runelite.client.plugins.testing.ethanapi.collections.NPCs;
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
import java.util.ArrayList;
import java.util.List;


@PluginDescriptor(
        name = ".B~Butterfly Catcher",
        description = "Catches butterfly you have the level for",
        tags = {"moth","hunter","Balaclava"},
        enabledByDefault = false
)
@Slf4j
public class BButterflyCatcherPlugin extends Plugin
{
    private final Client client = RuneLite.getInjector().getInstance(Client.class);

    @Override
    protected void startUp() throws Exception {
        if(client.getGameState().equals(GameState.LOGGED_IN)) {
            //change login timer to green when its on
            client.getWidget(10616865).setTextColor(Color.GREEN.getRGB());
        }
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

        if(client.getLocalPlayer().isInteracting()){
            return;
        }

        //if run is off, and we have some energy, turn run back on
        if(client.getVarpValue(173) != 1 && client.getEnergy() / 100 >= 10){
            Widgets.search().withId(10485787).first().ifPresent(runOrb->{
                ClickWidget.widgetAction(runOrb,"toggle run");
            });
        }

        List<Integer> butterflies = new ArrayList<>();
        for(Butterflies butterfly : Butterflies.values()){
            if(client.getBoostedSkillLevel(Skill.HUNTER) >= butterfly.getLevelRequired()){
                butterflies.add(butterfly.getId());
            }
        }

        NPCs.search().walkable().idInList(butterflies).noOneInteractingWith().nearestByPath().ifPresent(butterfly->{
            ClickNpc.npcAction(butterfly,"catch");
        });


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
