package net.swofty.type.skyblockgeneric.chocolatefactory.employee;

import lombok.Getter;
import net.minestom.server.item.Material;

/**
 * Represents the 7 employees that can be hired and upgraded in the Chocolate Factory.
 * Each employee produces chocolate per second based on their level.
 */
@Getter
public enum ChocolateEmployee {
    RABBIT_BRO(1, "Rabbit Bro", 1, Material.PLAYER_HEAD,
            "ewogICJ0aW1lc3RhbXAiIDogMTcxMjU5NDI0NjM2MywKICAicHJvZmlsZUlkIiA6ICJjZjc4YzFkZjE3ZTI0Y2Q5YTIxYmU4NWQ0NDk5ZWE4ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXR0c0FybW9yU3RhbmRzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI4NzkzNGJkZDlkZjI3MDViMjUxYmI5OTdlMDI5YjE4YzFlOTRkZjEyOTkyYjgxMDdlNzQ0OTdiMjA1Y2E3ZTgiCiAgICB9CiAgfQp9",
            "Ambition on two feet!"),
    RABBIT_COUSIN(2, "Rabbit Cousin", 2, Material.PLAYER_HEAD,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTcyZjZkYjI5YTc2ZGYxMmJiMjJjZGEzNWI5OTNlZmVkYWU4YjgxMTViNGU0MTkxY2YzYzYzMzdhYWY3ZjM5MiJ9fX0=",
            "Laid-back legend!"),
    RABBIT_SIS(3, "Rabbit Sis", 4, Material.PLAYER_HEAD,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIxNjY4ZWY3Y2I3OWRkOWMyMmNlM2QxZjNmNGNiNmUyNTU5ODkzYjc0NTk5NTEwZTMwM2Y0N2E3YzQ1MWNlMyJ9fX0=",
            "Rebel with a cause!"),
    RABBIT_DADDY(4, "Rabbit Daddy", 8, Material.PLAYER_HEAD,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTdlZDY2ZjVhNzAyMzk1MWE0NmI0ZTNkYzRlZDEwNGQ4ZmRlYWM1NDljMWEyMmMxMTk3MWVhMzVkNmI0YTFmMCJ9fX0=",
            "Stuck in a highlight reel!"),
    RABBIT_GRANNY(5, "Rabbit Granny", 16, Material.PLAYER_HEAD,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk4M2I2NTYxMTdiYmZiZjRlNTlkMmUxY2U5YzE4MjFiNzM3OGQ3MzlkZjY0YjNiNzY0NjI4ZGRjMTkwYzk0YSJ9fX0=",
            "Storyteller supreme!"),
    RABBIT_UNCLE(6, "Rabbit Uncle", 32, Material.PLAYER_HEAD,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNhMmFjN2Q2OGRjNDdkMzc4ZDE3NTYyOWM1MTdhNzMxMTlmZDA0YjA3MzNkYmUxMDU1M2FiMDFhNWRlNjJhZCJ9fX0=",
            "Stuck in a highlight reel!"),
    RABBIT_DOG(7, "Rabbit Dog", 64, Material.PLAYER_HEAD,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBkY2NkMjgzMTgxMTU3NThiZGQ5YTlhNjQ2YTgxYWEwNDkxOTNiZGVkNGZmYzI0ZmRhMjU2ZWI5MzgxMmRjMCJ9fX0=",
            "Making chocolate, not eating it!");

    private final int index;
    private final String displayName;
    private final int baseCpsPerLevel;
    private final Material material;
    private final String skinTexture;
    private final String flavor;

    ChocolateEmployee(int index, String displayName, int baseCpsPerLevel, Material material, String skinTexture, String flavor) {
        this.index = index;
        this.displayName = displayName;
        this.baseCpsPerLevel = baseCpsPerLevel;
        this.material = material;
        this.skinTexture = skinTexture;
        this.flavor = flavor;
    }

    /**
     * Gets the CpS produced per level for this employee
     */
    public int getCpsPerLevel() {
        return baseCpsPerLevel;
    }

    /**
     * Gets the total CpS this employee provides at a given level
     */
    public int getCpsAtLevel(int level) {
        return level * baseCpsPerLevel;
    }

    /**
     * Gets the employee by index (1-7)
     */
    public static ChocolateEmployee getByIndex(int index) {
        for (ChocolateEmployee employee : values()) {
            if (employee.getIndex() == index) {
                return employee;
            }
        }
        return null;
    }

    /**
     * Gets the color code for this employee's display
     */
    public String getColorCode() {
        return switch (this) {
            case RABBIT_BRO -> "§a";
            case RABBIT_COUSIN -> "§9";
            case RABBIT_SIS -> "§5";
            case RABBIT_DADDY -> "§6";
            case RABBIT_GRANNY -> "§d";
            case RABBIT_UNCLE -> "§b";
            case RABBIT_DOG -> "§c";
        };
    }

    /**
     * Gets the description for this employee
     */
    public String getDescription() {
        return switch (this) {
            case RABBIT_BRO -> "Your loyal assistant who loves chocolate.";
            case RABBIT_COUSIN -> "A distant relative with a nose for cocoa.";
            case RABBIT_SIS -> "She knows the secret family recipes.";
            case RABBIT_DADDY -> "The founder of your chocolate empire.";
            case RABBIT_GRANNY -> "Centuries of chocolate wisdom.";
            case RABBIT_UNCLE -> "He traveled the world for rare ingredients.";
            case RABBIT_DOG -> "Not actually a dog. Don't ask.";
        };
    }
}
