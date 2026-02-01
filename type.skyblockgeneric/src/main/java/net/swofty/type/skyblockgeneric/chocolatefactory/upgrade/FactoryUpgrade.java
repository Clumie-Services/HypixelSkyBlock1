package net.swofty.type.skyblockgeneric.chocolatefactory.upgrade;

import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateFactoryData;

/**
 * Interface for factory upgrades that can be purchased and upgraded.
 */
public interface FactoryUpgrade {

    /**
     * Gets the display name of this upgrade.
     */
    String getDisplayName();

    /**
     * Gets the description of what this upgrade does.
     */
    String getDescription();

    /**
     * Gets the maximum level of this upgrade.
     */
    int getMaxLevel();

    /**
     * Gets the minimum factory level required to unlock this upgrade.
     */
    int getRequiredFactoryLevel();

    /**
     * Gets the current level of this upgrade for the given player data.
     */
    int getCurrentLevel(ChocolateFactoryData data);

    /**
     * Sets the current level of this upgrade.
     */
    void setCurrentLevel(ChocolateFactoryData data, int level);

    /**
     * Calculates the cost to upgrade to the next level.
     */
    long getUpgradeCost(ChocolateFactoryData data);

    /**
     * Checks if this upgrade can be upgraded further.
     */
    default boolean canUpgrade(ChocolateFactoryData data) {
        return getCurrentLevel(data) < getMaxLevel() &&
               data.getFactoryLevel() >= getRequiredFactoryLevel();
    }

    /**
     * Gets the bonus description for the current level.
     */
    String getBonusDescription(ChocolateFactoryData data);
}
