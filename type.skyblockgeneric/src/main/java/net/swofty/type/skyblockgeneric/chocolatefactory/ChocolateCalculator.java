package net.swofty.type.skyblockgeneric.chocolatefactory;

import net.swofty.type.skyblockgeneric.chocolatefactory.employee.ChocolateEmployee;
import net.swofty.type.skyblockgeneric.chocolatefactory.rabbit.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;

/**
 * Handles all cost and production calculations for the Chocolate Factory.
 * All formulas are based on the official Hypixel SkyBlock wiki.
 */
public class ChocolateCalculator {

    // Cost multipliers for first 10 levels of Rabbit Bro
    private static final double[] RABBIT_BRO_MULTIPLIERS = {1, 2, 4, 6, 8, 9, 9.5, 10, 10.5, 11};

    // Cost multipliers for Time Tower levels
    private static final double[] TIME_TOWER_MULTIPLIERS = {0, 1, 2, 3, 4, 6, 8, 10, 12, 14, 16, 20, 24, 30, 40};

    // Production multiplier per factory level
    private static final double[] FACTORY_PRODUCTION_MULTIPLIERS = {1.0, 1.1, 1.25, 1.5, 2.0, 2.5};

    /**
     * Calculates the cost to upgrade an employee to the next level.
     *
     * @param employee The employee to upgrade
     * @param currentLevel The employee's current level
     * @param factoryLevel The player's factory level (1-6)
     * @return The chocolate cost for the upgrade
     */
    public static long calculateEmployeeUpgradeCost(ChocolateEmployee employee, int currentLevel, int factoryLevel) {
        int cf = factoryLevel;
        int level = currentLevel;
        int employeeIndex = employee.getIndex();

        // Special case: First 10 levels of Rabbit Bro
        if (employee == ChocolateEmployee.RABBIT_BRO && level < 10) {
            double baseCost = 30 + 20 * cf;
            double multiplier = RABBIT_BRO_MULTIPLIERS[level];
            return (long) (baseCost * multiplier);
        }

        // Standard formula for all other cases
        // base_cost = (216 + 144 * CF) * i^2 where i = employee index (1-7)
        // upgrade_cost = base_cost * 1.05^L
        double baseCost = (216 + 144 * cf) * Math.pow(employeeIndex, 2);
        double upgradeCost = baseCost * Math.pow(1.05, level);

        return (long) upgradeCost;
    }

    /**
     * Calculates the cost to upgrade the Time Tower to the next level.
     *
     * @param currentLevel Current Time Tower level (0-14)
     * @param factoryLevel The player's factory level (1-6)
     * @return The chocolate cost for the upgrade
     */
    public static long calculateTimeTowerCost(int currentLevel, int factoryLevel) {
        if (currentLevel >= 15 || currentLevel < 0) return Long.MAX_VALUE;

        int cf = factoryLevel;
        double baseCost = 4_500_000 + 500_000 * cf;
        double multiplier = TIME_TOWER_MULTIPLIERS[currentLevel + 1];

        return (long) (baseCost * multiplier);
    }

    /**
     * Calculates the cost to upgrade the Rabbit Barn to the next level.
     *
     * @param currentLevel Current Rabbit Barn level
     * @return The chocolate cost for the upgrade
     */
    public static long calculateRabbitBarnCost(int currentLevel) {
        // cost = floor(5000 * 1.05^(L-1)) for L >= 1
        // For level 0 -> 1, base cost is 5000
        if (currentLevel == 0) return 5000;
        return (long) Math.floor(5000 * Math.pow(1.05, currentLevel));
    }

    /**
     * Calculates the cost to upgrade Hand-Baked Chocolate to the next level.
     *
     * @param currentLevel Current Hand-Baked level (0-9)
     * @return The chocolate cost for the upgrade
     */
    public static long calculateHandBakedCost(int currentLevel) {
        if (currentLevel >= 10) return Long.MAX_VALUE;

        // Costs increase progressively
        long[] costs = {100, 500, 2_000, 10_000, 50_000, 200_000, 1_000_000, 5_000_000, 25_000_000, 100_000_000};
        return costs[currentLevel];
    }

    /**
     * Calculates the cost to upgrade the Rabbit Shrine to the next level.
     *
     * @param currentLevel Current Rabbit Shrine level (0-19)
     * @return The chocolate cost for the upgrade
     */
    public static long calculateRabbitShrineCost(int currentLevel) {
        if (currentLevel >= 20) return Long.MAX_VALUE;

        // Base cost with exponential scaling
        return (long) (500_000 * Math.pow(1.5, currentLevel));
    }

    /**
     * Calculates the cost to upgrade Coach Jackrabbit to the next level.
     *
     * @param currentLevel Current Coach Jackrabbit level (0-19)
     * @return The chocolate cost for the upgrade
     */
    public static long calculateCoachJackrabbitCost(int currentLevel) {
        if (currentLevel >= 20) return Long.MAX_VALUE;

        // Base cost with exponential scaling
        return (long) (1_000_000 * Math.pow(1.5, currentLevel));
    }

    /**
     * Calculates the chocolate required to prestige to the next factory level.
     *
     * @param currentFactoryLevel Current factory level (1-5)
     * @return The prestige chocolate requirement
     */
    public static long calculatePrestigeRequirement(int currentFactoryLevel) {
        return switch (currentFactoryLevel) {
            case 1 -> 150_000_000L;          // CF1 -> CF2
            case 2 -> 1_000_000_000L;        // CF2 -> CF3
            case 3 -> 4_000_000_000L;        // CF3 -> CF4
            case 4 -> 10_000_000_000L;       // CF4 -> CF5
            case 5 -> 50_000_000_000L;       // CF5 -> CF6
            default -> Long.MAX_VALUE;
        };
    }

    /**
     * Calculates the total base Chocolate per Second from all sources.
     *
     * @param data The player's chocolate factory data
     * @param collectedRabbits The set of rabbit IDs the player has collected
     * @return The base CpS before multipliers
     */
    public static double calculateBaseCps(ChocolateFactoryData data, Set<String> collectedRabbits) {
        double baseCps = 0;

        // Add CpS from employees
        for (ChocolateEmployee employee : ChocolateEmployee.values()) {
            int level = data.getEmployeeLevel(employee);
            baseCps += employee.getCpsAtLevel(level);
        }

        // Add CpS from collected rabbits
        for (String rabbitId : collectedRabbits) {
            ChocolateRabbit rabbit = ChocolateRabbit.getById(rabbitId);
            if (rabbit != null) {
                baseCps += rabbit.getRarity().getBaseCps();
            }
        }

        return baseCps;
    }

    /**
     * Calculates the total production multiplier from all sources.
     *
     * @param data The player's chocolate factory data
     * @param collectedRabbits The set of rabbit IDs the player has collected
     * @param hasMuRabbit Whether the player has the Mu rabbit (special Time Tower bonus)
     * @return The total multiplier
     */
    public static double calculateMultiplier(ChocolateFactoryData data, Set<String> collectedRabbits, boolean hasMuRabbit) {
        double multiplier = 1.0;

        // Add multiplier from collected rabbits
        for (String rabbitId : collectedRabbits) {
            ChocolateRabbit rabbit = ChocolateRabbit.getById(rabbitId);
            if (rabbit != null) {
                multiplier += rabbit.getRarity().getMultiplierBonus();
            }
        }

        // Add Coach Jackrabbit bonus (1% per level)
        multiplier += data.getCoachJackrabbitLevel() * 0.01;

        // Add Time Tower bonus if active (10% per level)
        if (data.isTimeTowerActive()) {
            multiplier += data.getTimeTowerLevel() * 0.1;

            // Mu rabbit gives +70% during Time Tower
            if (hasMuRabbit) {
                multiplier += 0.7;
            }
        }

        return multiplier;
    }

    /**
     * Calculates the factory production multiplier based on factory level.
     *
     * @param factoryLevel The factory level (1-6)
     * @return The production multiplier
     */
    public static double getFactoryProductionMultiplier(int factoryLevel) {
        if (factoryLevel < 1 || factoryLevel > 6) return 1.0;
        return FACTORY_PRODUCTION_MULTIPLIERS[factoryLevel - 1];
    }

    /**
     * Calculates the total CpS for a player.
     *
     * @param data The player's chocolate factory data
     * @param collectedRabbits The set of rabbit IDs the player has collected
     * @param hasMuRabbit Whether the player has the Mu rabbit
     * @return The total chocolate per second
     */
    public static double calculateTotalCps(ChocolateFactoryData data, Set<String> collectedRabbits, boolean hasMuRabbit) {
        double baseCps = calculateBaseCps(data, collectedRabbits);
        double multiplier = calculateMultiplier(data, collectedRabbits, hasMuRabbit);
        double productionMultiplier = getFactoryProductionMultiplier(data.getFactoryLevel());

        return baseCps * multiplier * productionMultiplier;
    }

    /**
     * Calculates the total CpS for a SkyBlockPlayer.
     *
     * @param player The player
     * @return The total chocolate per second
     */
    public static double calculateTotalCps(SkyBlockPlayer player) {
        ChocolateFactoryData data = player.getChocolateFactoryData();
        Set<String> rabbits = player.getHoppityCollectionData().getCollectedRabbitIds();
        boolean hasMu = rabbits.contains("MU");
        return calculateTotalCps(data, rabbits, hasMu);
    }

    /**
     * Calculates the base CpS for a SkyBlockPlayer.
     *
     * @param player The player
     * @return The base chocolate per second before multipliers
     */
    public static double calculateBaseCps(SkyBlockPlayer player) {
        ChocolateFactoryData data = player.getChocolateFactoryData();
        Set<String> rabbits = player.getHoppityCollectionData().getCollectedRabbitIds();
        return calculateBaseCps(data, rabbits);
    }

    /**
     * Calculates the multiplier bonus from collected rabbits only.
     *
     * @param collectedRabbits The set of rabbit IDs the player has collected
     * @return The rabbit collection multiplier
     */
    public static double calculateRabbitMultiplier(Set<String> collectedRabbits) {
        double multiplier = 1.0;
        for (String rabbitId : collectedRabbits) {
            ChocolateRabbit rabbit = ChocolateRabbit.getById(rabbitId);
            if (rabbit != null) {
                multiplier += rabbit.getRarity().getMultiplierBonus();
            }
        }
        return multiplier;
    }

    /**
     * Formats a large number into a readable string (e.g., 1.5M, 2.3B).
     *
     * @param value The number to format
     * @return The formatted string
     */
    public static String formatNumber(long value) {
        if (value < 1000) return String.valueOf(value);
        if (value < 1_000_000) return String.format("%.1fK", value / 1000.0);
        if (value < 1_000_000_000) return String.format("%.1fM", value / 1_000_000.0);
        if (value < 1_000_000_000_000L) return String.format("%.1fB", value / 1_000_000_000.0);
        return String.format("%.1fT", value / 1_000_000_000_000.0);
    }

    /**
     * Formats a CpS value into a readable string.
     *
     * @param cps The CpS value
     * @return The formatted string
     */
    public static String formatCps(double cps) {
        if (cps < 1000) return String.format("%.1f", cps);
        if (cps < 1_000_000) return String.format("%.1fK", cps / 1000.0);
        if (cps < 1_000_000_000) return String.format("%.1fM", cps / 1_000_000.0);
        return String.format("%.1fB", cps / 1_000_000_000.0);
    }

    /**
     * Gets the display name for a factory level.
     *
     * @param factoryLevel The factory level (1-6)
     * @return The display name
     */
    public static String getFactoryLevelName(int factoryLevel) {
        return switch (factoryLevel) {
            case 1 -> "§aChocolate Factory I";
            case 2 -> "§9Chocolate Factory II";
            case 3 -> "§5Chocolate Factory III";
            case 4 -> "§6Chocolate Factory IV";
            case 5 -> "§dChocolate Factory V";
            case 6 -> "§cChocolate Factory VI";
            default -> "§7Chocolate Factory";
        };
    }

    /**
     * Gets the color code for a factory level.
     *
     * @param factoryLevel The factory level (1-6)
     * @return The color code
     */
    public static String getFactoryLevelColor(int factoryLevel) {
        return switch (factoryLevel) {
            case 1 -> "§a";
            case 2 -> "§9";
            case 3 -> "§5";
            case 4 -> "§6";
            case 5 -> "§d";
            case 6 -> "§c";
            default -> "§7";
        };
    }
}
