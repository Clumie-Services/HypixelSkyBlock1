package net.swofty.type.skyblockgeneric.chocolatefactory.rabbit;

import lombok.Getter;

/**
 * Represents the 7 rarity tiers of chocolate rabbits.
 * Each rarity provides different CpS and multiplier bonuses.
 */
@Getter
public enum ChocolateRabbitRarity {
    COMMON("Common", "§f", 1, 0.002, 0),
    UNCOMMON("Uncommon", "§a", 2, 0.003, 0),
    RARE("Rare", "§9", 4, 0.005, 0),
    EPIC("Epic", "§5", 8, 0.01, 0.001),
    LEGENDARY("Legendary", "§6", 16, 0.02, 0.002),
    MYTHIC("Mythic", "§d", 32, 0.03, 0.003),
    DIVINE("Divine", "§b", 64, 0.05, 0.005);

    private final String displayName;
    private final String colorCode;
    private final int baseCps;
    private final double multiplierBonus;
    private final double extraMultiplier; // For duplicate rabbits

    ChocolateRabbitRarity(String displayName, String colorCode, int baseCps, double multiplierBonus, double extraMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.baseCps = baseCps;
        this.multiplierBonus = multiplierBonus;
        this.extraMultiplier = extraMultiplier;
    }

    /**
     * Gets the formatted display name with color code
     */
    public String getFormattedName() {
        return colorCode + displayName;
    }

    /**
     * Gets the chocolate reward for finding a rabbit of this rarity
     */
    public long getChocolateReward() {
        return switch (this) {
            case COMMON -> 2_500;
            case UNCOMMON -> 5_000;
            case RARE -> 10_000;
            case EPIC -> 25_000;
            case LEGENDARY -> 100_000;
            case MYTHIC -> 500_000;
            case DIVINE -> 1_000_000;
        };
    }

    /**
     * Gets the duplicate chocolate reward (when finding a rabbit you already have)
     */
    public long getDuplicateChocolateReward() {
        return switch (this) {
            case COMMON -> 250;
            case UNCOMMON -> 500;
            case RARE -> 1_000;
            case EPIC -> 2_500;
            case LEGENDARY -> 10_000;
            case MYTHIC -> 50_000;
            case DIVINE -> 100_000;
        };
    }

    /**
     * Gets the base weight for spawning rabbits of this rarity
     * Lower weight = rarer
     */
    public int getSpawnWeight() {
        return switch (this) {
            case COMMON -> 100;
            case UNCOMMON -> 50;
            case RARE -> 25;
            case EPIC -> 10;
            case LEGENDARY -> 3;
            case MYTHIC -> 1;
            case DIVINE -> 0; // Divine rabbits cannot spawn naturally
        };
    }

    /**
     * Gets the rarity from a string name
     */
    public static ChocolateRabbitRarity fromString(String name) {
        for (ChocolateRabbitRarity rarity : values()) {
            if (rarity.name().equalsIgnoreCase(name) || rarity.displayName.equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return COMMON;
    }

    /**
     * Gets the ordinal-based comparison value (higher = rarer)
     */
    public int getRarityValue() {
        return ordinal();
    }

    /**
     * Checks if this rarity is at least as rare as another
     */
    public boolean isAtLeast(ChocolateRabbitRarity other) {
        return this.ordinal() >= other.ordinal();
    }
}
