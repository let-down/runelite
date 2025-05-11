package net.runelite.client.plugins.testing.balaclavaapi.utility;

import net.runelite.client.plugins.testing.balaclavaapi.utility.DoAction;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.RuneLite;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClickNpc {
    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static void npcAction(NPC targetNpc, String action){
        if(targetNpc == null){
            System.out.println("Npc action attempted with null npc");
            return;
        }

        NPCComposition comp = client.getNpcDefinition(targetNpc.getId());
        if (comp == null) {
            return;
        }
        if (comp.getActions() == null) {
            return;
        }
        List<String> npcActions = Arrays.stream(comp.getActions()).collect(Collectors.toList());
        for (int i = 0; i < npcActions.size(); i++) {
            if (npcActions.get(i) == null)
                continue;
            npcActions.set(i, npcActions.get(i).toLowerCase());
        }
        int num = -1;
        for (String npcAction : npcActions) {
            if (action.equalsIgnoreCase(npcAction)) {
                num = npcActions.indexOf(action.toLowerCase()) + 1;
            }
        }

        if (num < 1 || num > 10) {
            return;
        }
        Point clickPoint = DoAction.getClickPoint(targetNpc);
        DoAction.action(0,0, MenuAction.of(8+num) ,targetNpc.getIndex(),-1,"", clickPoint.getX(), clickPoint.getY());

    }





}
