package net.swofty.type.skyblockgeneric.chocolatefactory.milestone;

import lombok.Getter;

/**
 * Represents the 24 shop milestones based on chocolate spent at the shop.
 */
@Getter
public enum ShopMilestone {
    MILESTONE_1(50_000L, "First Purchase", "§7Unlocked basic shop items."),
    MILESTONE_2(100_000L, "Returning Customer", "§7+§a5% §7shop discount."),
    MILESTONE_3(250_000L, "Regular Shopper", "§7Unlocked more shop items."),
    MILESTONE_4(500_000L, "Valued Customer", "§7+§a5% §7shop discount."),
    MILESTONE_5(1_000_000L, "Shop Regular", "§7Unlocked premium shop items."),
    MILESTONE_6(2_500_000L, "Big Spender", "§7+§a5% §7shop discount."),
    MILESTONE_7(5_000_000L, "VIP Customer", "§7Unlocked exclusive shop items."),
    MILESTONE_8(10_000_000L, "Elite Shopper", "§7+§a5% §7shop discount."),
    MILESTONE_9(25_000_000L, "Shop Champion", "§7Unlocked rare shop items."),
    MILESTONE_10(50_000_000L, "Chocolate Collector", "§7+§a5% §7shop discount."),
    MILESTONE_11(100_000_000L, "Master Shopper", "§7Unlocked legendary shop items."),
    MILESTONE_12(250_000_000L, "Shop Legend", "§7+§a5% §7shop discount."),
    MILESTONE_13(500_000_000L, "Retail Royalty", "§7Unlocked mythic shop items."),
    MILESTONE_14(1_000_000_000L, "Shopping Spree", "§7+§a10% §7shop discount."),
    MILESTONE_15(2_000_000_000L, "Consumer King", "§7Unlocked divine shop items."),
    MILESTONE_16(3_500_000_000L, "Shopper Supreme", "§7+§a10% §7shop discount."),
    MILESTONE_17(5_000_000_000L, "Mall Magnate", "§7Special shop benefits."),
    MILESTONE_18(7_500_000_000L, "Retail Baron", "§7+§a10% §7shop discount."),
    MILESTONE_19(10_000_000_000L, "Shopping Tycoon", "§7Exclusive shop perks."),
    MILESTONE_20(15_000_000_000L, "Consumer Emperor", "§7+§a10% §7shop discount."),
    MILESTONE_21(25_000_000_000L, "Shopping Overlord", "§7Premium shop access."),
    MILESTONE_22(40_000_000_000L, "Retail God", "§7+§a10% §7shop discount."),
    MILESTONE_23(60_000_000_000L, "Shopping Deity", "§7Ultimate shop benefits."),
    MILESTONE_24(100_000_000_000L, "Shop Master", "§7Maximum shop discount achieved!");

    private final long chocolateRequired;
    private final String name;
    private final String reward;

    ShopMilestone(long chocolateRequired, String name, String reward) {
        this.chocolateRequired = chocolateRequired;
        this.name = name;
        this.reward = reward;
    }

    /**
     * Gets the milestone number (1-24)
     */
    public int getNumber() {
        return ordinal() + 1;
    }

    /**
     * Gets the formatted display name
     */
    public String getFormattedName() {
        return "§6Shop Milestone " + getNumber() + ": §e" + name;
    }

    /**
     * Checks if a player has reached this milestone
     */
    public boolean isReached(long shopSpent) {
        return shopSpent >= chocolateRequired;
    }

    /**
     * Gets the progress percentage towards this milestone
     */
    public double getProgress(long shopSpent) {
        if (isReached(shopSpent)) return 100.0;
        return (double) shopSpent / chocolateRequired * 100;
    }

    /**
     * Gets the next milestone after this one
     */
    public ShopMilestone getNext() {
        int nextOrdinal = ordinal() + 1;
        if (nextOrdinal >= values().length) return null;
        return values()[nextOrdinal];
    }

    /**
     * Gets the previous milestone before this one
     */
    public ShopMilestone getPrevious() {
        if (ordinal() == 0) return null;
        return values()[ordinal() - 1];
    }

    /**
     * Gets the current milestone for a given shop spent amount
     */
    public static ShopMilestone getCurrentMilestone(long shopSpent) {
        ShopMilestone current = null;
        for (ShopMilestone milestone : values()) {
            if (milestone.isReached(shopSpent)) {
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
    public static ShopMilestone getNextMilestone(long shopSpent) {
        for (ShopMilestone milestone : values()) {
            if (!milestone.isReached(shopSpent)) {
                return milestone;
            }
        }
        return null;
    }

    /**
     * Gets the number of milestones reached
     */
    public static int getMilestonesReached(long shopSpent) {
        int count = 0;
        for (ShopMilestone milestone : values()) {
            if (milestone.isReached(shopSpent)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculates the total shop discount based on milestones reached
     */
    public static double getTotalDiscount(long shopSpent) {
        double discount = 0;
        for (ShopMilestone milestone : values()) {
            if (!milestone.isReached(shopSpent)) break;

            // Parse discount from reward string
            String reward = milestone.getReward();
            if (reward.contains("shop discount")) {
                if (reward.contains("+§a5%")) {
                    discount += 0.05;
                } else if (reward.contains("+§a10%")) {
                    discount += 0.10;
                }
            }
        }
        return Math.min(discount, 0.50); // Cap at 50% discount
    }
}
