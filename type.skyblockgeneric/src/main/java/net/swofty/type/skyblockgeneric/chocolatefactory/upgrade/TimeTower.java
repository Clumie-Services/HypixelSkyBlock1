package net.swofty.type.skyblockgeneric.chocolatefactory.upgrade;

import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateCalculator;
import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateFactoryData;

/**
 * Time Tower upgrade implementation.
 * When activated, boosts chocolate production for 1 hour.
 * Charges accumulate every 8 hours (max 3).
 */
public class TimeTower implements FactoryUpgrade {

    @Override
    public String getDisplayName() {
        return "§bTime Tower";
    }

    @Override
    public String getDescription() {
        return "When activated, boosts chocolate production for 1 hour!";
    }

    @Override
    public int getMaxLevel() {
        return 15;
    }

    @Override
    public int getRequiredFactoryLevel() {
        return 2; // Unlocked at CF2
    }

    @Override
    public int getCurrentLevel(ChocolateFactoryData data) {
        return data.getTimeTowerLevel();
    }

    @Override
    public void setCurrentLevel(ChocolateFactoryData data, int level) {
        data.setTimeTowerLevel(level);
    }

    @Override
    public long getUpgradeCost(ChocolateFactoryData data) {
        return ChocolateCalculator.calculateTimeTowerCost(
                data.getTimeTowerLevel(),
                data.getFactoryLevel()
        );
    }

    @Override
    public String getBonusDescription(ChocolateFactoryData data) {
        int level = getCurrentLevel(data);
        int bonusPercent = level * 10;
        return "§6+" + bonusPercent + "% §7production when active";
    }

    /**
     * Gets the production bonus multiplier for this Time Tower level.
     */
    public double getProductionBonus(int level) {
        return level * 0.10; // 10% per level
    }

    /**
     * Gets the current number of charges.
     */
    public int getCharges(ChocolateFactoryData data) {
        return data.getTimeTowerCharges();
    }

    /**
     * Checks if Time Tower is currently active.
     */
    public boolean isActive(ChocolateFactoryData data) {
        return data.isTimeTowerActive();
    }

    /**
     * Gets remaining active time in milliseconds.
     */
    public long getRemainingTime(ChocolateFactoryData data) {
        return data.getTimeTowerRemainingMs();
    }

    /**
     * Activates the Time Tower if charges are available.
     * @return true if activated successfully
     */
    public boolean activate(ChocolateFactoryData data) {
        return data.activateTimeTower();
    }

    /**
     * Duration of Time Tower activation in milliseconds (1 hour).
     */
    public static final long ACTIVATION_DURATION_MS = 60 * 60 * 1000L;

    /**
     * Time between charge accumulations in milliseconds (8 hours).
     */
    public static final long CHARGE_INTERVAL_MS = 8 * 60 * 60 * 1000L;

    /**
     * Maximum number of charges that can be stored.
     */
    public static final int MAX_CHARGES = 3;
}
