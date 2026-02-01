package net.swofty.type.skyblockgeneric.chocolatefactory.milestone;

import lombok.Getter;

/**
 * Represents the 26 factory milestones based on all-time chocolate produced.
 */
@Getter
public enum FactoryMilestone {
    MILESTONE_1(100_000L, "Chocolate Apprentice", "§7Unlocks the ability to hire §aRabbit Bro§7."),
    MILESTONE_2(500_000L, "Cocoa Novice", "§7+§a10% §7base chocolate from clicks."),
    MILESTONE_3(1_000_000L, "Chocolate Maker", "§7Unlocks the ability to hire §9Rabbit Cousin§7."),
    MILESTONE_4(2_500_000L, "Truffle Hunter", "§7+§a5% §7stray rabbit spawn rate."),
    MILESTONE_5(5_000_000L, "Sweet Tooth", "§7Unlocks the ability to hire §5Rabbit Sis§7."),
    MILESTONE_6(10_000_000L, "Confectionery Expert", "§7+§a10% §7base chocolate from clicks."),
    MILESTONE_7(25_000_000L, "Chocolate Artisan", "§7Unlocks the ability to hire §6Rabbit Daddy§7."),
    MILESTONE_8(50_000_000L, "Master Chocolatier", "§7+§a5% §7stray rabbit spawn rate."),
    MILESTONE_9(100_000_000L, "Cocoa Baron", "§7Unlocks the ability to hire §dRabbit Gramps§7."),
    MILESTONE_10(150_000_000L, "Prestige I Available", "§7You can now prestige to §aChocolate Factory II§7!"),
    MILESTONE_11(250_000_000L, "Chocolate Tycoon", "§7Unlocks the ability to hire §bRabbit Uncle§7."),
    MILESTONE_12(500_000_000L, "Sweet Empire", "§7+§a10% §7base chocolate from clicks."),
    MILESTONE_13(750_000_000L, "Cocoa Kingdom", "§7Unlocks the ability to hire §cRabbit Dog§7."),
    MILESTONE_14(1_000_000_000L, "Prestige II Available", "§7You can now prestige to §9Chocolate Factory III§7!"),
    MILESTONE_15(1_500_000_000L, "Chocolate Dynasty", "§7+§a5% §7stray rabbit spawn rate."),
    MILESTONE_16(2_500_000_000L, "Cocoa Overlord", "§7+§a10% §7base chocolate from clicks."),
    MILESTONE_17(4_000_000_000L, "Prestige III Available", "§7You can now prestige to §5Chocolate Factory IV§7!"),
    MILESTONE_18(6_000_000_000L, "Sweet Sovereign", "§7+§a5% §7stray rabbit spawn rate."),
    MILESTONE_19(8_000_000_000L, "Chocolate Emperor", "§7+§a10% §7base chocolate from clicks."),
    MILESTONE_20(10_000_000_000L, "Prestige IV Available", "§7You can now prestige to §6Chocolate Factory V§7!"),
    MILESTONE_21(15_000_000_000L, "Cocoa Conqueror", "§7+§a5% §7stray rabbit spawn rate."),
    MILESTONE_22(25_000_000_000L, "Chocolate Legend", "§7+§a10% §7base chocolate from clicks."),
    MILESTONE_23(40_000_000_000L, "Sweet Immortal", "§7+§a5% §7stray rabbit spawn rate."),
    MILESTONE_24(50_000_000_000L, "Prestige V Available", "§7You can now prestige to §dChocolate Factory VI§7!"),
    MILESTONE_25(75_000_000_000L, "Cocoa God", "§7+§a10% §7base chocolate from clicks."),
    MILESTONE_26(100_000_000_000L, "Chocolate Deity", "§7You have mastered the art of chocolate!");

    private final long chocolateRequired;
    private final String name;
    private final String reward;

    FactoryMilestone(long chocolateRequired, String name, String reward) {
        this.chocolateRequired = chocolateRequired;
        this.name = name;
        this.reward = reward;
    }

    /**
     * Gets the milestone number (1-26)
     */
    public int getNumber() {
        return ordinal() + 1;
    }

    /**
     * Gets the formatted display name
     */
    public String getFormattedName() {
        return "§6Milestone " + getNumber() + ": §e" + name;
    }

    /**
     * Checks if a player has reached this milestone
     */
    public boolean isReached(long allTimeChocolate) {
        return allTimeChocolate >= chocolateRequired;
    }

    /**
     * Gets the progress percentage towards this milestone
     */
    public double getProgress(long allTimeChocolate) {
        if (isReached(allTimeChocolate)) return 100.0;
        return (double) allTimeChocolate / chocolateRequired * 100;
    }

    /**
     * Gets the next milestone after this one
     */
    public FactoryMilestone getNext() {
        int nextOrdinal = ordinal() + 1;
        if (nextOrdinal >= values().length) return null;
        return values()[nextOrdinal];
    }

    /**
     * Gets the previous milestone before this one
     */
    public FactoryMilestone getPrevious() {
        if (ordinal() == 0) return null;
        return values()[ordinal() - 1];
    }

    /**
     * Gets the current milestone for a given all-time chocolate amount
     */
    public static FactoryMilestone getCurrentMilestone(long allTimeChocolate) {
        FactoryMilestone current = null;
        for (FactoryMilestone milestone : values()) {
            if (milestone.isReached(allTimeChocolate)) {
                current = milestone;
            } else {
                break;
            }
        }
        return current;
    }

    /**
     * Gets the next milestone to reach
     */
    public static FactoryMilestone getNextMilestone(long allTimeChocolate) {
        for (FactoryMilestone milestone : values()) {
            if (!milestone.isReached(allTimeChocolate)) {
                return milestone;
            }
        }
        return null;
    }

    /**
     * Gets the number of milestones reached
     */
    public static int getMilestonesReached(long allTimeChocolate) {
        int count = 0;
        for (FactoryMilestone milestone : values()) {
            if (milestone.isReached(allTimeChocolate)) {
                count++;
            }
        }
        return count;
    }
}
