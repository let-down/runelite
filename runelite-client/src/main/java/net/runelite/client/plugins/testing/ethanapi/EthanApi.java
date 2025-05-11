package net.runelite.client.plugins.testing.ethanapi;

import net.runelite.client.plugins.testing.ethanapi.collections.*;
import net.runelite.client.plugins.testing.ethanapi.collections.query.QuickPrayer;
import net.runelite.client.plugins.testing.ethanapi.pathfinding.Node;
import net.runelite.client.plugins.testing.balaclavaapi.utility.DoAction;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientUI;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class EthanApi {

    static boolean initialized = false;

    public static void init(){

        if (initialized) {
            return;
        }
        EventBus eventBus = RuneLite.getInjector().getInstance(EventBus.class);
        eventBus.register(RuneLite.getInjector().getInstance(Inventory.class));
        eventBus.register(RuneLite.getInjector().getInstance(Bank.class));
        eventBus.register(RuneLite.getInjector().getInstance(BankInventory.class));
        eventBus.register(RuneLite.getInjector().getInstance(NPCs.class));
        eventBus.register(RuneLite.getInjector().getInstance(TileObjects.class));
        eventBus.register(RuneLite.getInjector().getInstance(Players.class));
        eventBus.register(RuneLite.getInjector().getInstance(Equipment.class));
        eventBus.register(RuneLite.getInjector().getInstance(DepositBox.class));
        eventBus.register(RuneLite.getInjector().getInstance(ShopInventory.class));
        eventBus.register(RuneLite.getInjector().getInstance(Shop.class));
        eventBus.register(RuneLite.getInjector().getInstance(DoAction.class));
        initialized = true;
    }

    static ClientUI clientUI = RuneLite.getInjector().getInstance(ClientUI.class);
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    static PluginManager pluginManager = RuneLite.getInjector().getInstance(PluginManager.class);
    static ItemManager itemManager = RuneLite.getInjector().getInstance(ItemManager.class);
    static final HashSet<WorldPoint> EMPTY_SET = new HashSet<>();
    public static final int[][] directionsMap = {
            {-2, 0},
            {0, 2},
            {2, 0},
            {0, -2},
            {1, 0},
            {0, 1},
            {-1, 0},
            {0, -1},
            {1, 1},
            {-1, -1},
            {-1, 1},
            {1, -1},
            {-2, 2},
            {-2, -2},
            {2, 2},
            {2, -2},
            {-2, -1},
            {-2, 1},
            {-1, -2},
            {-1, 2},
            {1, -2},
            {1, 2},
            {2, -1},
            {2, 1}
    };
    public static LoadingCache<Integer, ItemComposition> itemDefs = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Integer, ItemComposition>() {
                        @Override
                        public ItemComposition load(Integer itemId) {
                            return itemManager.getItemComposition(itemId);
                        }
                    });

    private static WorldPoint playerPosition() {
        return client.getLocalPlayer().getWorldLocation();
    }

    public static List<WorldPoint> sceneWorldPoints() {
        List<WorldPoint> allWorldPoints = new ArrayList<>();
        Scene scene = client.getTopLevelWorldView().getScene();
        Tile[][][] tiles = scene.getTiles();
        int z = client.getTopLevelWorldView().getPlane();
        for (int x = 0; x < 104; ++x) {
            for (int y = 0; y < 104; ++y) {
                Tile tile = tiles[z][x][y];
                if (tile == null) {
                    continue;
                }
                allWorldPoints.add(tile.getWorldLocation());
            }
        }
        return allWorldPoints;
    }

    public static List<WorldPoint> reachableTiles() {
        boolean[][] visited = new boolean[104][104];
        int[][] flags = client.getTopLevelWorldView().getCollisionMaps()[client.getTopLevelWorldView().getPlane()].getFlags();
        WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();
        int firstPoint = (playerLoc.getX()-client.getTopLevelWorldView().getBaseX() << 16) | playerLoc.getY()-client.getTopLevelWorldView().getBaseY();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(firstPoint);

        while (!queue.isEmpty()) {
            int point = queue.poll();
            short x =(short)(point >> 16);
            short y = (short)point;
            if (y < 0 || x < 0 || y > 104 || x > 104) {
                continue;
            }

            // Get the current WorldPoint


            if ((flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH) == 0 && (flags[x][y - 1] & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0 && !visited[x][y - 1]) {
                queue.add((x << 16) | (y - 1));
                visited[x][y - 1] = true;
            }
            if ((flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_NORTH) == 0 && (flags[x][y + 1] & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0 && !visited[x][y + 1]) {
                queue.add((x << 16) | (y + 1));
                visited[x][y + 1] = true;
            }
            if ((flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_WEST) == 0 && (flags[x - 1][y] & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0 && !visited[x - 1][y]) {
                queue.add(((x - 1) << 16) | y);
                visited[x - 1][y] = true;
            }
            if ((flags[x][y] & CollisionDataFlag.BLOCK_MOVEMENT_EAST) == 0 && (flags[x + 1][y] & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0 && !visited[x + 1][y]) {
                queue.add(((x + 1) << 16) | y);
                visited[x + 1][y] = true;
            }
        }
        int baseX = client.getTopLevelWorldView().getBaseX();
        int baseY = client.getTopLevelWorldView().getBaseY();
        int plane = client.getTopLevelWorldView().getPlane();
        List<WorldPoint> finalPoints = new ArrayList<>();
        for (int x = 0; x < 104; ++x) {
            for (int y = 0; y < 104; ++y) {
                if (visited[x][y]) {
                    finalPoints.add(new WorldPoint(baseX + x, baseY + y, plane));
                }
            }
        }
        return finalPoints;
    }

    static boolean canMoveWest(int flag) {
        return (flag & CollisionDataFlag.BLOCK_MOVEMENT_WEST) != CollisionDataFlag.BLOCK_MOVEMENT_WEST;
    }

    static boolean canMoveEast(int flag) {
        return (flag & CollisionDataFlag.BLOCK_MOVEMENT_EAST) != CollisionDataFlag.BLOCK_MOVEMENT_EAST;
    }

    static boolean canMoveNorth(int flag) {
        return (flag & CollisionDataFlag.BLOCK_MOVEMENT_NORTH) != CollisionDataFlag.BLOCK_MOVEMENT_NORTH;
    }

    static boolean canMoveSouth(int flag) {
        return (flag & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH) != CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;
    }

    static boolean canMoveTo(int flag) {
        if ((flag & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == CollisionDataFlag.BLOCK_MOVEMENT_FULL) {
            return false;
        }
        if ((flag & CollisionDataFlag.BLOCK_MOVEMENT_FLOOR) == CollisionDataFlag.BLOCK_MOVEMENT_FLOOR) {
            return false;
        }
        if ((flag & CollisionDataFlag.BLOCK_MOVEMENT_OBJECT) == CollisionDataFlag.BLOCK_MOVEMENT_OBJECT) {
            return false;
        }
        return (flag & CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION) != CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION;
    }



    public static boolean isMoving() {
        return client.getLocalPlayer().getPoseAnimation()
                != client.getLocalPlayer().getIdlePoseAnimation();
    }
    public static PathResult canPathToTile(WorldPoint destinationTile) {
        int z = client.getPlane();
        if (z != destinationTile.getPlane()) {
            return new PathResult(false, Integer.MAX_VALUE);
        }

        CollisionData[] collisionData = client.getCollisionMaps();
        if (collisionData == null) {
            return new PathResult(false, Integer.MAX_VALUE);
        }

        int[][] directions = new int[128][128];
        int[][] distances = new int[128][128];
        int[] bufferX = new int[4096];
        int[] bufferY = new int[4096];

        // Initialise directions and distances
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                directions[i][j] = 0;
                distances[i][j] = Integer.MAX_VALUE;
            }
        }

        int pSX = client.getLocalPlayer().getLocalLocation().getSceneX();

        int pSY = client.getLocalPlayer().getLocalLocation().getSceneY();
        Point p1 = client.getScene().getTiles()[client.getPlane()][pSX][pSY].getSceneLocation();
        LocalPoint lp = LocalPoint.fromWorld(client, destinationTile);
        if (lp == null || !lp.isInScene()) {
            return new PathResult(false, Integer.MAX_VALUE);
        }
        Point p2 = new Point(lp.getSceneX(), lp.getSceneY());

        int middleX = p1.getX();
        int middleY = p1.getY();
        int currentX = middleX;
        int currentY = middleY;
        int offsetX = 64;
        int offsetY = 64;
        // Initialise directions and distances for starting tile
        directions[offsetX][offsetY] = 99;
        distances[offsetX][offsetY] = 0;
        int index1 = 0;
        bufferX[0] = currentX;
        int index2 = 1;
        bufferY[0] = currentY;
        int[][] collisionDataFlags = collisionData[z].getFlags();

        int currentDistance = Integer.MAX_VALUE;
        boolean isReachable = false;

        while (index1 != index2) {
            currentX = bufferX[index1];
            currentY = bufferY[index1];
            index1 = index1 + 1 & 4095;
            // currentX is for the local coordinate while currentMapX is for the index in the directions and distances arrays
            int currentMapX = currentX - middleX + offsetX;
            int currentMapY = currentY - middleY + offsetY;
            if ((currentX == p2.getX()) && (currentY == p2.getY())) {
                isReachable = true;
                break;
            }

            currentDistance = distances[currentMapX][currentMapY] + 1;
            if (currentMapX > 0 && directions[currentMapX - 1][currentMapY] == 0 && (collisionDataFlags[currentX - 1][currentY] & 19136776) == 0) {
                // Able to move 1 tile west
                bufferX[index2] = currentX - 1;
                bufferY[index2] = currentY;
                index2 = index2 + 1 & 4095;
                directions[currentMapX - 1][currentMapY] = 2;
                distances[currentMapX - 1][currentMapY] = currentDistance;
            }

            if (currentMapX < 127 && directions[currentMapX + 1][currentMapY] == 0 && (collisionDataFlags[currentX + 1][currentY] & 19136896) == 0) {
                // Able to move 1 tile east
                bufferX[index2] = currentX + 1;
                bufferY[index2] = currentY;
                index2 = index2 + 1 & 4095;
                directions[currentMapX + 1][currentMapY] = 8;
                distances[currentMapX + 1][currentMapY] = currentDistance;
            }

            if (currentMapY > 0 && directions[currentMapX][currentMapY - 1] == 0 && (collisionDataFlags[currentX][currentY - 1] & 19136770) == 0) {
                // Able to move 1 tile south
                bufferX[index2] = currentX;
                bufferY[index2] = currentY - 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX][currentMapY - 1] = 1;
                distances[currentMapX][currentMapY - 1] = currentDistance;
            }

            if (currentMapY < 127 && directions[currentMapX][currentMapY + 1] == 0 && (collisionDataFlags[currentX][currentY + 1] & 19136800) == 0) {
                // Able to move 1 tile north
                bufferX[index2] = currentX;
                bufferY[index2] = currentY + 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX][currentMapY + 1] = 4;
                distances[currentMapX][currentMapY + 1] = currentDistance;
            }

            if (currentMapX > 0 && currentMapY > 0 && directions[currentMapX - 1][currentMapY - 1] == 0 && (collisionDataFlags[currentX - 1][currentY - 1] & 19136782) == 0 && (collisionDataFlags[currentX - 1][currentY] & 19136776) == 0 && (collisionDataFlags[currentX][currentY - 1] & 19136770) == 0) {
                // Able to move 1 tile south-west
                bufferX[index2] = currentX - 1;
                bufferY[index2] = currentY - 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX - 1][currentMapY - 1] = 3;
                distances[currentMapX - 1][currentMapY - 1] = currentDistance;
            }

            if (currentMapX < 127 && currentMapY > 0 && directions[currentMapX + 1][currentMapY - 1] == 0 && (collisionDataFlags[currentX + 1][currentY - 1] & 19136899) == 0 && (collisionDataFlags[currentX + 1][currentY] & 19136896) == 0 && (collisionDataFlags[currentX][currentY - 1] & 19136770) == 0) {
                // Able to move 1 tile north-west
                bufferX[index2] = currentX + 1;
                bufferY[index2] = currentY - 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX + 1][currentMapY - 1] = 9;
                distances[currentMapX + 1][currentMapY - 1] = currentDistance;
            }

            if (currentMapX > 0 && currentMapY < 127 && directions[currentMapX - 1][currentMapY + 1] == 0 && (collisionDataFlags[currentX - 1][currentY + 1] & 19136824) == 0 && (collisionDataFlags[currentX - 1][currentY] & 19136776) == 0 && (collisionDataFlags[currentX][currentY + 1] & 19136800) == 0) {
                // Able to move 1 tile south-east
                bufferX[index2] = currentX - 1;
                bufferY[index2] = currentY + 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX - 1][currentMapY + 1] = 6;
                distances[currentMapX - 1][currentMapY + 1] = currentDistance;
            }

            if (currentMapX < 127 && currentMapY < 127 && directions[currentMapX + 1][currentMapY + 1] == 0 && (collisionDataFlags[currentX + 1][currentY + 1] & 19136992) == 0 && (collisionDataFlags[currentX + 1][currentY] & 19136896) == 0 && (collisionDataFlags[currentX][currentY + 1] & 19136800) == 0) {
                // Able to move 1 tile north-east
                bufferX[index2] = currentX + 1;
                bufferY[index2] = currentY + 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX + 1][currentMapY + 1] = 12;
                distances[currentMapX + 1][currentMapY + 1] = currentDistance;
            }
        }
        return new PathResult(isReachable, currentDistance);
    }

    public static PathResult canPathToTile(WorldPoint startTile, WorldPoint destinationTile) {
        int z = client.getPlane();
        if (z != destinationTile.getPlane()) {
            return new PathResult(false, Integer.MAX_VALUE);
        }

        CollisionData[] collisionData = client.getCollisionMaps();
        if (collisionData == null) {
            return new PathResult(false, Integer.MAX_VALUE);
        }

        int[][] directions = new int[128][128];
        int[][] distances = new int[128][128];
        int[] bufferX = new int[4096];
        int[] bufferY = new int[4096];

        // Initialise directions and distances
        for (int i = 0; i < 128; ++i) {
            for (int j = 0; j < 128; ++j) {
                directions[i][j] = 0;
                distances[i][j] = Integer.MAX_VALUE;
            }
        }

        LocalPoint starter = LocalPoint.fromWorld(client, startTile);
        if(starter == null){
            return new PathResult(false, Integer.MAX_VALUE);
        }

        int pSX = starter.getSceneX();
        int pSY = starter.getSceneY();

        Point p1 = client.getScene().getTiles()[client.getPlane()][pSX][pSY].getSceneLocation();
        LocalPoint lp = LocalPoint.fromWorld(client, destinationTile);
        if (lp == null || !lp.isInScene()) {
            return new PathResult(false, Integer.MAX_VALUE);
        }
        Point p2 = new Point(lp.getSceneX(), lp.getSceneY());

        int middleX = p1.getX();
        int middleY = p1.getY();
        int currentX = middleX;
        int currentY = middleY;
        int offsetX = 64;
        int offsetY = 64;
        // Initialise directions and distances for starting tile
        directions[offsetX][offsetY] = 99;
        distances[offsetX][offsetY] = 0;
        int index1 = 0;
        bufferX[0] = currentX;
        int index2 = 1;
        bufferY[0] = currentY;
        int[][] collisionDataFlags = collisionData[z].getFlags();

        int currentDistance = Integer.MAX_VALUE;
        boolean isReachable = false;

        while (index1 != index2) {
            currentX = bufferX[index1];
            currentY = bufferY[index1];
            index1 = index1 + 1 & 4095;
            // currentX is for the local coordinate while currentMapX is for the index in the directions and distances arrays
            int currentMapX = currentX - middleX + offsetX;
            int currentMapY = currentY - middleY + offsetY;
            if ((currentX == p2.getX()) && (currentY == p2.getY())) {
                isReachable = true;
                break;
            }

            currentDistance = distances[currentMapX][currentMapY] + 1;
            if (currentMapX > 0 && directions[currentMapX - 1][currentMapY] == 0 && (collisionDataFlags[currentX - 1][currentY] & 19136776) == 0) {
                // Able to move 1 tile west
                bufferX[index2] = currentX - 1;
                bufferY[index2] = currentY;
                index2 = index2 + 1 & 4095;
                directions[currentMapX - 1][currentMapY] = 2;
                distances[currentMapX - 1][currentMapY] = currentDistance;
            }

            if (currentMapX < 127 && directions[currentMapX + 1][currentMapY] == 0 && (collisionDataFlags[currentX + 1][currentY] & 19136896) == 0) {
                // Able to move 1 tile east
                bufferX[index2] = currentX + 1;
                bufferY[index2] = currentY;
                index2 = index2 + 1 & 4095;
                directions[currentMapX + 1][currentMapY] = 8;
                distances[currentMapX + 1][currentMapY] = currentDistance;
            }

            if (currentMapY > 0 && directions[currentMapX][currentMapY - 1] == 0 && (collisionDataFlags[currentX][currentY - 1] & 19136770) == 0) {
                // Able to move 1 tile south
                bufferX[index2] = currentX;
                bufferY[index2] = currentY - 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX][currentMapY - 1] = 1;
                distances[currentMapX][currentMapY - 1] = currentDistance;
            }

            if (currentMapY < 127 && directions[currentMapX][currentMapY + 1] == 0 && (collisionDataFlags[currentX][currentY + 1] & 19136800) == 0) {
                // Able to move 1 tile north
                bufferX[index2] = currentX;
                bufferY[index2] = currentY + 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX][currentMapY + 1] = 4;
                distances[currentMapX][currentMapY + 1] = currentDistance;
            }

            if (currentMapX > 0 && currentMapY > 0 && directions[currentMapX - 1][currentMapY - 1] == 0 && (collisionDataFlags[currentX - 1][currentY - 1] & 19136782) == 0 && (collisionDataFlags[currentX - 1][currentY] & 19136776) == 0 && (collisionDataFlags[currentX][currentY - 1] & 19136770) == 0) {
                // Able to move 1 tile south-west
                bufferX[index2] = currentX - 1;
                bufferY[index2] = currentY - 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX - 1][currentMapY - 1] = 3;
                distances[currentMapX - 1][currentMapY - 1] = currentDistance;
            }

            if (currentMapX < 127 && currentMapY > 0 && directions[currentMapX + 1][currentMapY - 1] == 0 && (collisionDataFlags[currentX + 1][currentY - 1] & 19136899) == 0 && (collisionDataFlags[currentX + 1][currentY] & 19136896) == 0 && (collisionDataFlags[currentX][currentY - 1] & 19136770) == 0) {
                // Able to move 1 tile north-west
                bufferX[index2] = currentX + 1;
                bufferY[index2] = currentY - 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX + 1][currentMapY - 1] = 9;
                distances[currentMapX + 1][currentMapY - 1] = currentDistance;
            }

            if (currentMapX > 0 && currentMapY < 127 && directions[currentMapX - 1][currentMapY + 1] == 0 && (collisionDataFlags[currentX - 1][currentY + 1] & 19136824) == 0 && (collisionDataFlags[currentX - 1][currentY] & 19136776) == 0 && (collisionDataFlags[currentX][currentY + 1] & 19136800) == 0) {
                // Able to move 1 tile south-east
                bufferX[index2] = currentX - 1;
                bufferY[index2] = currentY + 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX - 1][currentMapY + 1] = 6;
                distances[currentMapX - 1][currentMapY + 1] = currentDistance;
            }

            if (currentMapX < 127 && currentMapY < 127 && directions[currentMapX + 1][currentMapY + 1] == 0 && (collisionDataFlags[currentX + 1][currentY + 1] & 19136992) == 0 && (collisionDataFlags[currentX + 1][currentY] & 19136896) == 0 && (collisionDataFlags[currentX][currentY + 1] & 19136800) == 0) {
                // Able to move 1 tile north-east
                bufferX[index2] = currentX + 1;
                bufferY[index2] = currentY + 1;
                index2 = index2 + 1 & 4095;
                directions[currentMapX + 1][currentMapY + 1] = 12;
                distances[currentMapX + 1][currentMapY + 1] = currentDistance;
            }
        }
        return new PathResult(isReachable, currentDistance);
    }


    public static boolean canPathToArea(WorldPoint startPoint, List<WorldPoint> destinationTiles) {
        for(WorldPoint worldPoint : destinationTiles){
            if(canPathToTile(startPoint,worldPoint).isReachable()){
                return true;
            }
        }

        return false;
    }

    public static boolean canPathToArea(List<WorldPoint> startArea, List<WorldPoint> destinationTiles) {
        for(WorldPoint worldPoint2 :startArea){
            for(WorldPoint worldPoint : destinationTiles) {
                if (canPathToTile(worldPoint2, worldPoint).isReachable()) {
                    return true;
                }
            }
        }

        return false;
    }


    public static class PathResult {
        private final boolean reachable;
        private final int distance;

        public PathResult(boolean reachable, int distance) {
            this.reachable = reachable;
            this.distance = distance;
        }

        public boolean isReachable() {
            return reachable;
        }

        public int getDistance() {
            return distance;
        }
    }

    @SneakyThrows
    public static void stopPlugin(Plugin plugin) {

        SwingUtilities.invokeAndWait(() ->
        {
            try {
                pluginManager.stopPlugin(plugin);
                pluginManager.setPluginEnabled(plugin, false);
            } catch (PluginInstantiationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Client getClient() {
        return client;
    }

    public static ClientUI getClientUI() {
        return clientUI;
    }


    public static List<WorldPoint> pathToGoalSetFromPlayerUsingReachableTiles(HashSet<WorldPoint> goalSet, HashSet<WorldPoint> dangerous, HashSet<WorldPoint> impassible) {
        return pathToGoalSet(goalSet, dangerous, impassible, new HashSet<>(reachableTiles()), playerPosition());
    }

    public static List<WorldPoint> pathToGoalSetFromPlayerNoCustomTiles(HashSet<WorldPoint> goalSet) {
        return pathToGoalSet(goalSet, EMPTY_SET, EMPTY_SET, new HashSet<>(reachableTiles()), playerPosition());
    }
    public static List<WorldPoint> pathToGoalFromPlayerUsingCustomDangerous(WorldPoint goal, HashSet<WorldPoint> dangerous) {
        return pathToGoalSet(new HashSet<>(List.of(goal)), dangerous, EMPTY_SET, new HashSet<>(reachableTiles()), playerPosition());
    }

    public static List<WorldPoint> pathToGoalFromPlayerUsingReachableTiles(WorldPoint goal, HashSet<WorldPoint> dangerous, HashSet<WorldPoint> impassible) {
        return pathToGoalSet(new HashSet<>(List.of(goal)), dangerous, impassible, new HashSet<>(reachableTiles()), playerPosition());
    }

    public static List<WorldPoint> pathToGoalFromPlayerNoCustomTiles(WorldPoint goal) {
        return pathToGoalSet(new HashSet<>(List.of(goal)), EMPTY_SET, EMPTY_SET, new HashSet<>(reachableTiles()), playerPosition());
    }

    public static List<WorldPoint> pathToGoalSet(HashSet<WorldPoint> goalSet, HashSet<WorldPoint> dangerous, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable, WorldPoint starting) {
        if (Collections.disjoint(goalSet, walkable)) {
            return null;
        }
        ArrayDeque<Node> queue = new ArrayDeque<Node>();
        HashSet<WorldPoint> visited = new HashSet<>();
        visited.add(starting);
        queue.add(new Node(starting));
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (goalSet.contains(current.getData())) {
                List<WorldPoint> ret = new ArrayList<>();
                while (current != null) {
                    ret.add(current.getData());
                    current = current.getPrevious();
                }
                Collections.reverse(ret);
                ret.remove(0);
                return ret;
            }
            for (int[] direction : directionsMap) {
                int x = direction[0];
                int y = direction[1];
                if (x == 0 && y == 0) {
                    continue;
                }
                WorldPoint currentPoint = current.getData();
                WorldPoint nextPoint = current.getData().dy(y).dx(x);
                if (!walkable.contains(nextPoint) || impassible.contains(nextPoint) || dangerous.contains(nextPoint) || visited.contains(nextPoint)) {
                    continue;
                }
                if (x == -2 && y == 0) {
                    if (farWObstructed(currentPoint, impassible, walkable)) {
                        continue;
                    }
                    visited.add(nextPoint);
                    queue.add(new Node(nextPoint, current));
                    continue;
                }
                //Far East
                if (x == 2 && y == 0) {
                    if (farEObstructed(currentPoint, impassible, walkable)) {
                        continue;
                    }
                    visited.add(nextPoint);
                    queue.add(new Node(nextPoint, current));
                    continue;
                }
                //Far South
                if (x == 0 && y == -2) {
                    if (farSObstructed(currentPoint, impassible, walkable)) {
                        continue;
                    }
                    visited.add(nextPoint);
                    queue.add(new Node(nextPoint, current));
                    continue;
                }
                //Far North
                if (x == 0 && y == 2) {
                    if (farNObstructed(currentPoint, impassible, walkable)) {
                        continue;
                    }
                    visited.add(nextPoint);
                    queue.add(new Node(nextPoint, current));
                    continue;
                }
                //far movements
                //L movement in here so i dont get lost in the saauce down there
                if (Math.abs(x) + Math.abs(y) == 3) {
                    //North east
                    if (x == 1 && y == 2) {
                        if (northEastLObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //East north
                    if (x == 2 && y == 1) {
                        if (eastNorthLObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //East south
                    if (x == 2 && y == -1) {
                        if (eastSouthLObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //South east
                    if (x == 1 && y == -2) {
                        if (southEastLObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //South west
                    if (x == -1 && y == -2) {
                        if (southWestLObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //West south
                    if (x == -2 && y == -1) {
                        if (westSouthLObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //West north
                    if (x == -2 && y == 1) {
                        if (westNorthLObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //North west
                    if (x == -1 && y == 2) {
                        if (northWestLObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                } else {
                    //One tile movement

                    //diagonal SE
                    if (x == 1 && y == -1) {
                        if (seObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //diagonal NE
                    if (x == 1 && y == 1) {
                        if (neObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //diagonal NW
                    if (x == -1 && y == 1) {
                        if (nwObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //diagonal SW
                    if (x == -1 && y == -1) {
                        if (swObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }

                    //Two tile movement

                    //Diagonal SW
                    if (x == -2 && y == -2) {
                        if (farSWObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //Diagonal NW
                    if (x == -2 && y == 2) {
                        if (farNWObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //Diagonal SE
                    if (x == 2 && y == -2) {
                        if (farSEObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                    //Diagonal NE
                    if (x == 2 && y == 2) {
                        if (farNEObstructed(currentPoint, impassible, walkable)) {
                            continue;
                        }
                        visited.add(nextPoint);
                        queue.add(new Node(nextPoint, current));
                        continue;
                    }
                }
            }
        }
        return null;
    }

    static boolean nwObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(-1).dy(0)) || !walkable.contains(starting.dx(-1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(0).dy(1)) || !walkable.contains(starting.dx(0).dy(1));
    }

    public static void sendClientMessage(String message) {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
    }

    static boolean neObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(1).dy(0)) || !walkable.contains(starting.dx(1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(0).dy(1)) || !walkable.contains(starting.dx(0).dy(1));
    }

    //L movement of 2 north one east, as opposed to 2 east one north (what should we call this difference?)
    static boolean northEastLObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(1).dy(1)) || !walkable.contains(starting.dx(1).dy(1))) {
            return true;
        }
        if (impassible.contains(starting.dx(0).dy(1)) || !walkable.contains(starting.dx(0).dy(1))) {
            return true;
        }
        return impassible.contains(starting.dx(0).dy(2)) || !walkable.contains(starting.dx(0).dy(2));
    }

    //L movement of 2 east one North, as opposed to 2 north one east
    static boolean eastNorthLObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(1).dy(1)) || !walkable.contains(starting.dx(1).dy(1))) {
            return true;
        }
        if (impassible.contains(starting.dx(1).dy(0)) || !walkable.contains(starting.dx(1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(2).dy(0)) || !walkable.contains(starting.dx(2).dy(0));
    }

    static boolean eastSouthLObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(1).dy(-1)) || !walkable.contains(starting.dx(1).dy(-1))) {
            return true;
        }
        if (impassible.contains(starting.dx(1).dy(0)) || !walkable.contains(starting.dx(1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(2).dy(0)) || !walkable.contains(starting.dx(2).dy(0));
    }

    static boolean southEastLObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(1).dy(-1)) || !walkable.contains(starting.dx(1).dy(-1))) {
            return true;
        }
        if (impassible.contains(starting.dx(0).dy(-1)) || !walkable.contains(starting.dx(0).dy(-1))) {
            return true;
        }
        return impassible.contains(starting.dx(0).dy(-2)) || !walkable.contains(starting.dx(0).dy(-2));
    }

    static boolean southWestLObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(-1).dy(-1)) || !walkable.contains(starting.dx(-1).dy(-1))) {
            return true;
        }
        if (impassible.contains(starting.dx(0).dy(-1)) || !walkable.contains(starting.dx(0).dy(-1))) {
            return true;
        }
        return impassible.contains(starting.dx(0).dy(-2)) || !walkable.contains(starting.dx(0).dy(-2));
    }

    static boolean westSouthLObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(-1).dy(-1)) || !walkable.contains(starting.dx(-1).dy(-1))) {
            return true;
        }
        if (impassible.contains(starting.dx(-1).dy(0)) || !walkable.contains(starting.dx(-1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(-2).dy(0)) || !walkable.contains(starting.dx(-2).dy(0));
    }

    static boolean westNorthLObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(-1).dy(1)) || !walkable.contains(starting.dx(-1).dy(1))) {
            return true;
        }
        if (impassible.contains(starting.dx(-1).dy(0)) || !walkable.contains(starting.dx(-1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(-2).dy(0)) || !walkable.contains(starting.dx(-2).dy(0));
    }

    static boolean northWestLObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(-1).dy(1)) || !walkable.contains(starting.dx(-1).dy(1))) {
            return true;
        }
        if (impassible.contains(starting.dx(0).dy(1)) || !walkable.contains(starting.dx(0).dy(1))) {
            return true;
        }
        return impassible.contains(starting.dx(0).dy(2)) || !walkable.contains(starting.dx(0).dy(2));
    }

    static boolean seObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(1).dy(0)) || !walkable.contains(starting.dx(1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(0).dy(-1)) || !walkable.contains(starting.dx(0).dy(-1));
    }

    static boolean swObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(-1).dy(0)) || !walkable.contains(starting.dx(-1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(0).dy(-1)) || !walkable.contains(starting.dx(0).dy(-1));
    }

    static boolean farNObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        return impassible.contains(starting.dx(0).dy(1)) || !walkable.contains(starting.dx(0).dy(1));
    }

    static boolean farSObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        return impassible.contains(starting.dx(0).dy(-1)) || !walkable.contains(starting.dx(0).dy(-1));
    }

    static boolean farEObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        return impassible.contains(starting.dx(1).dy(0)) || !walkable.contains(starting.dx(1).dy(0));
    }

    static boolean farWObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        return impassible.contains(starting.dx(-1).dy(0)) || !walkable.contains(starting.dx(-1).dy(0));
    }

    static boolean farSWObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(-1).dy(-2)) || !walkable.contains(starting.dx(-1).dy(-2))) {
            return true;
        }
        if (impassible.contains(starting.dx(-2).dy(-1)) || !walkable.contains(starting.dx(-2).dy(-1))) {
            return true;
        }
        if (impassible.contains(starting.dx(0).dy(-1)) || !walkable.contains(starting.dx(0).dy(-1))) {
            return true;
        }
        if (impassible.contains(starting.dx(-1).dy(0)) || !walkable.contains(starting.dx(-1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(-1).dy(-1)) || !walkable.contains(starting.dx(-1).dy(-1));
    }

    static boolean farNWObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(-1).dy(2)) || !walkable.contains(starting.dx(-1).dy(2))) {
            return true;
        }
        if (impassible.contains(starting.dx(-2).dy(1)) || !walkable.contains(starting.dx(-2).dy(1))) {
            return true;
        }
        if (impassible.contains(starting.dx(0).dy(1)) || !walkable.contains(starting.dx(0).dy(1))) {
            return true;
        }
        if (impassible.contains(starting.dx(-1).dy(0)) || !walkable.contains(starting.dx(-1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(-1).dy(1)) || !walkable.contains(starting.dx(-1).dy(1));
    }

    static boolean farNEObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(1).dy(2)) || !walkable.contains(starting.dx(1).dy(2))) {
            return true;
        }
        if (impassible.contains(starting.dx(2).dy(1)) || !walkable.contains(starting.dx(2).dy(1))) {
            return true;
        }
        if (impassible.contains(starting.dx(0).dy(1)) || !walkable.contains(starting.dx(0).dy(1))) {
            return true;
        }
        if (impassible.contains(starting.dx(1).dy(0)) || !walkable.contains(starting.dx(1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(1).dy(1)) || !walkable.contains(starting.dx(1).dy(1));
    }

    static boolean farSEObstructed(WorldPoint starting, HashSet<WorldPoint> impassible, HashSet<WorldPoint> walkable) {
        if (impassible.contains(starting.dx(1).dy(-2)) || !walkable.contains(starting.dx(1).dy(-2))) {
            return true;
        }
        if (impassible.contains(starting.dx(2).dy(-1)) || !walkable.contains(starting.dx(2).dy(-1))) {
            return true;
        }
        if (impassible.contains(starting.dx(0).dy(-1)) || !walkable.contains(starting.dx(0).dy(-1))) {
            return true;
        }
        if (impassible.contains(starting.dx(1).dy(0)) || !walkable.contains(starting.dx(1).dy(0))) {
            return true;
        }
        return impassible.contains(starting.dx(1).dy(-1)) || !walkable.contains(starting.dx(1).dy(-1));
    }





}
