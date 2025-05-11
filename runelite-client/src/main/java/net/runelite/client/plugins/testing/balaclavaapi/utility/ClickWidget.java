package net.runelite.client.plugins.testing.balaclavaapi.utility;

import net.runelite.client.plugins.testing.balaclavaapi.utility.DoAction;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClickWidget {

    public static void widgetAction(Widget widget, String... actionlist) {
        if (widget == null) {
            System.out.println("Widget null when attempting to do a widget action");
            return;
        }
        List<String> actions = Arrays.stream(widget.getActions()).collect(Collectors.toList());
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i) == null)
                continue;
            actions.set(i, actions.get(i).toLowerCase());
        }
        int num = -1;
        for (String action : actions) {
            for (String action2 : actionlist) {
                if (action != null && Text.removeTags(action).equalsIgnoreCase(action2)) {
                    num = actions.indexOf(action.toLowerCase()) + 1;
                }
            }
        }

        if (num < 1 || num > 10) {
            return;
        }

        Point clickPoint = DoAction.getClickPoint(widget);
        DoAction.action(widget.getIndex(),widget.getId(), MenuAction.CC_OP,num,widget.getItemId(),widget.getName(), clickPoint.getX(), clickPoint.getY());


    }
    public static void clickWidget(Widget widget, MenuAction action,int identifier) {
        if (widget == null) {
            System.out.println("Widget null when attempting to do a widget click");
            return;
        }


        Point clickPoint = DoAction.getClickPoint(widget);
        DoAction.action(widget.getIndex(),widget.getId(), action,identifier,widget.getItemId(),widget.getName(), clickPoint.getX(), clickPoint.getY());

    }

    public static void clickWidget(int p0, Widget widget, MenuAction action,int identifier) {
        if (widget == null) {
            System.out.println("Widget null when attempting to do a widget click");
            return;
        }


        Point clickPoint = DoAction.getClickPoint(widget);
        DoAction.action(p0,widget.getId(), action,identifier,widget.getItemId(),widget.getName(), clickPoint.getX(), clickPoint.getY());

    }
    public static void widgetOnWidget(Widget srcWidget, Widget targetWidget) {
        if (srcWidget == null) {
            System.out.println("Source Widget null when attempting to use on another widget");
            return;
        }

        if (targetWidget == null) {
            System.out.println("Target Widget null when attempting to use on another widget");
            return;
        }

        Point srcClickPoint = DoAction.getClickPoint(srcWidget);
        DoAction.action(srcWidget.getIndex(),srcWidget.getId(), MenuAction.WIDGET_TARGET,srcWidget.getId(),srcWidget.getItemId(),srcWidget.getName(), srcClickPoint.getX(), srcClickPoint.getY());
        Point targetClickPoint = DoAction.getClickPoint(targetWidget);
        DoAction.action(targetWidget.getIndex(),targetWidget.getId(), MenuAction.WIDGET_TARGET_ON_WIDGET,targetWidget.getId(),targetWidget.getItemId(),targetWidget.getName(), targetClickPoint.getX(), targetClickPoint.getY());



    }

    public static void widgetOnObject(Widget widget, TileObject obj){

        if(widget == null || obj == null){
            return;
        }

        ClickWidget.clickWidget(widget.getIndex(),widget,MenuAction.WIDGET_TARGET,0);


        Point clickPoint = DoAction.getClickPoint(obj);

        if (obj instanceof GameObject) {
            GameObject g = (GameObject) obj;
            DoAction.action(g.getSceneMinLocation().getX(), g.getSceneMinLocation().getY(), MenuAction.WIDGET_TARGET_ON_GAME_OBJECT, g.getId(), -1, "", clickPoint.getX(), clickPoint.getY());
        }else {
            DoAction.action(obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY(), MenuAction.WIDGET_TARGET_ON_GAME_OBJECT, obj.getId(), -1, "", clickPoint.getX(), clickPoint.getY());
        }

    }

    public static void widgetOnNpc(Widget widget, NPC npc){

        if(widget == null || npc == null){
            return;
        }

        ClickWidget.clickWidget(widget.getIndex(),widget,MenuAction.WIDGET_TARGET,0);


        int clickX = -1;
        int clickY = -1;

        if(npc.getCanvasTilePoly().getBounds() != null) {
            clickX = (int) (int)npc.getCanvasTilePoly().getBounds().getMinX();
            clickY = (int) (int)npc.getCanvasTilePoly().getBounds().getMinY();
        }
        DoAction.action(0,0, MenuAction.WIDGET_TARGET_ON_NPC ,npc.getIndex(),-1,"<col=ff9040>"+ widget.getName() +"</col><col=ffffff> -> <col=ffff00>" + npc.getName(),clickX,clickY);


    }



}
