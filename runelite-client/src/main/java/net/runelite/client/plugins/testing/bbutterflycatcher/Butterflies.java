package net.runelite.client.plugins.testing.bbutterflycatcher;

import net.runelite.api.NpcID;

public enum Butterflies
{
    RUBY_HARVEST(NpcID.RUBY_HARVEST, 15),
    SAPPHIRE_GLACIALIS(NpcID.SAPPHIRE_GLACIALIS, 25),
    SNOWY_KNIGHT(NpcID.SNOWY_KNIGHT, 35),
    BLACK_WARLOCK(NpcID.BLACK_WARLOCK, 45),
    SUNLIGHT_MOTH(NpcID.SUNLIGHT_MOTH, 65),
    MOONLIGHT_MOTH(NpcID.MOONLIGHT_MOTH,75);

    private final int id;
    private final int levelRequired;

    Butterflies(int id, int levelRequired) {
        this.id = id;
        this.levelRequired = levelRequired;
    }

    public int getId() {
        return id;
    }

    public int getLevelRequired() {
        return levelRequired;
    }

    @Override
    public String toString() {
        return id + " (Level required: " + levelRequired + ")";
    }

    public static void main(String[] args) {
        for (Butterflies butterfly : Butterflies.values()) {
            System.out.println(butterfly);
        }
    }
}
