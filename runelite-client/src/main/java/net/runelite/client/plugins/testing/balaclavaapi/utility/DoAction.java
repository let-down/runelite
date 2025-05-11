package net.runelite.client.plugins.testing.balaclavaapi.utility;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.testing.balaclavaapi.utility.coords.Tiles;
import net.runelite.client.plugins.testing.ethanapi.collections.ETileItem;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

public class DoAction {
    static boolean consume = false;
    static Client client = RuneLite.getInjector().getInstance(Client.class);

    static Random random = new Random();



    private static boolean printMenuActions = false;
    public static void setPrintMenuActions(boolean printMenuActions) {
        DoAction.printMenuActions = printMenuActions;
    }

    public static void click(int x, int y) {
        consume = true;
        sendMouseEventClick(new Point(x,y));
    }

    public static void action(int p1, int p2, MenuAction menuAction, int id, int itemId, String target,int x,int y){
        click(x,y);

        // good but sends red clicks to 0,0
        client.menuAction(p1,p2,menuAction,id,itemId,"Balaclava",target);

        //doesn't send red clicks to 0,0 but is sketchy?
//        invoke(p1,p2,menuAction.getId(),id,itemId,"Balaclava",target,x,y);
    }

    public static void invoke(int param0, int param1, int opcode, int identifier, int itemid, String option, String target, int x, int y) {
            try {

                Class<?> classWithMenuAction = client.getClass().getClassLoader().loadClass("pb");

                Method menuAction = Arrays.stream(classWithMenuAction.getDeclaredMethods())
                        .filter(method ->
                                method.getName().equals("ll")
                                        && method.getParameterCount() >= 10)
                        .findAny()
                        .orElse(null);

                if (menuAction == null) {
                    return;
                }

                // When invoking, static methods need null as the first parameter
                menuAction.setAccessible(true);

                // Determine the number of expected parameters
                int expectedArguments = menuAction.getParameterCount();
                System.out.println("Expected arguments: " + expectedArguments);

                // Prepare array of arguments to pass to invoke
                Object[] arguments = new Object[expectedArguments];
                arguments[0] = param0;
                arguments[1] = param1;
                arguments[2] = opcode;
                arguments[3] = identifier;
                arguments[4] = itemid;
                arguments[5] = -1;
                arguments[6] = option;
                arguments[7] = target;
                arguments[8] = x;
                arguments[9] = y;

                if (expectedArguments > 10) {
                    arguments[10] = (byte) -1259406248;
                }

                // Invoke the method with the prepared arguments
                Object result = menuAction.invoke(null, arguments);


                menuAction.setAccessible(false);
            } catch (Exception e) {
                System.out.println("Failed to invoke menu action.");
            }
        }




    private static void sendMouseEventClick(Point p) {
        long time = System.currentTimeMillis();
        Canvas canvas = client.getCanvas();
        MouseEvent press = new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, time, 0, p.getX(), p.getY(), 1, false);
        canvas.dispatchEvent(press);
        MouseEvent release = new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, time, 0,p.getX(), p.getY(), 1, false);
        canvas.dispatchEvent(release);
    }


    //---keyboard
    @Subscribe public void onMenuOptionClicked(MenuOptionClicked event){


        consumeClicks(event);

        if(printMenuActions && !event.isConsumed()) {
            System.out.println(event);
        }
    }

    public static void consumeClicks(MenuOptionClicked event){
        if (event.getMenuEntry().getIdentifier() == 0 && event.getMenuEntry().getParam0() == 0 && event.getParam1() == 0 && event.getItemId() == -1 && event.getMenuOption().equals("Walk here") && event.getId() == 0) {
            event.consume();
            return;
        }

        if(consume && !event.getMenuOption().equals("Balaclava")){
            event.consume();
           consume = false;
        }
    }

    public static Point getClickPoint(Widget widget){
        Point originalLocation = widget.getCanvasLocation();

        if(originalLocation == null){
            return new Point(0,0);
        }
        int width = widget.getWidth();
        int height = widget.getHeight();


        int modifiedX = originalLocation.getX() + random.nextInt(width);
        int modifiedY = originalLocation.getY() + random.nextInt(height);

        return new Point(modifiedX,modifiedY);


    }
    public static Point getClickPoint(NPC npc){


        LocalPoint ll = npc.getLocalLocation();
        Shape clickbox = Perspective.getClickbox(client, npc.getModel(), npc.getOrientation(),npc.getLocalLocation().getX(),npc.getLocalLocation().getY(),Perspective.getTileHeight(client, ll, npc.getWorldLocation().getPlane()));

        if(clickbox == null || clickbox.getBounds() == null){
            return new Point(0,0);
        }

        int clickX = (int) (int)clickbox.getBounds().getMinX();
        int clickY = (int) (int)npc.getCanvasTilePoly().getBounds().getMinY();


        int modifiedX = clickX + random.nextInt(clickbox.getBounds().width);
        int modifiedY = clickY + random.nextInt(clickbox.getBounds().height);



        return new Point(modifiedX,modifiedY);

    }
    public static Point getClickPoint(TileObject tileObject){
        Shape clickBox = tileObject.getClickbox();
        if(clickBox == null) {
            return new Point(0,0);
        }

        int clickX = (int) clickBox.getBounds2D().getMinX();
        int clickY = (int) clickBox.getBounds2D().getMinY();

        int modifiedX = clickX + random.nextInt((int)clickBox.getBounds2D().getWidth());
        int modifiedY = clickY + random.nextInt((int)clickBox.getBounds2D().getHeight());



        return new Point(modifiedX,modifiedY);

    }

    public static Point getClickPoint(ETileItem tileItem) {
        if (tileItem == null) {
            return new Point(0, 0);
        } else {
            Tile tile = Tiles.getAt(tileItem.getLocation());
            ItemLayer itemLayer = tile.getItemLayer();
            Shape clickBox = tile.getItemLayer().getClickbox();
            if (itemLayer == null) {
                return new Point(0, 0);
            } else {
                itemLayer.getClickbox();
                if (clickBox == null) {
                    return new Point(0, 0);
                } else {
                    int clickX = (int)clickBox.getBounds2D().getMinX();
                    int clickY = (int)clickBox.getBounds2D().getMinY();
                    int modifiedX = clickX + random.nextInt((int)clickBox.getBounds2D().getWidth());
                    int modifiedY = clickY + random.nextInt((int)clickBox.getBounds2D().getHeight());
                    return new Point(modifiedX, modifiedY);
                }
            }
        }
    }

}
