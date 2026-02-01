package net.swofty.type.skyblockgeneric.chocolatefactory;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Scheduled task that handles chocolate production for all online players.
 * Runs every second (20 ticks) to:
 * - Calculate and add CpS to player balances
 * - Update Time Tower charges (8h accumulation)
 * - Check Time Tower expiration (1h duration)
 */
public class ChocolateProductionTask {

    private static boolean initialized = false;

    /**
     * Starts the chocolate production scheduler.
     * Should be called once during server initialization.
     */
    public static void startProductionLoop() {
        if (initialized) return;
        initialized = true;

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            try {
                processAllPlayers();
            } catch (Exception e) {
                System.err.println("[ChocolateFactory] Error in production task: " + e.getMessage());
                e.printStackTrace();
            }
            return TaskSchedule.tick(20); // Run every second (20 ticks)
        });

        System.out.println("[ChocolateFactory] Production task started!");
    }

    /**
     * Processes chocolate production for all online players.
     */
    private static void processAllPlayers() {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            try {
                processPlayer(player);
            } catch (Exception e) {
                System.err.println("[ChocolateFactory] Error processing player " + player.getUsername() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Processes chocolate production for a single player.
     */
    private static void processPlayer(SkyBlockPlayer player) {
        ChocolateFactoryData data = player.getChocolateFactoryData();
        boolean dataChanged = false;

        // Update Time Tower charges
        int oldCharges = data.getTimeTowerCharges();
        data.updateTimeTowerCharges();
        if (data.getTimeTowerCharges() != oldCharges) {
            dataChanged = true;
        }

        // Check Time Tower expiration
        // (isTimeTowerActive() already handles expiration check internally)

        // Calculate CpS and add to balance
        double cps = ChocolateCalculator.calculateTotalCps(player);
        if (cps > 0) {
            // Add whole chocolate (truncate decimal)
            long chocolateToAdd = (long) cps;
            if (chocolateToAdd > 0) {
                data.addChocolate(chocolateToAdd);
                dataChanged = true;
            }
        }

        // Save data if changed
        if (dataChanged) {
            player.getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                    .setValue(data);
        }
    }

    /**
     * Manually processes a single player's chocolate production.
     * Can be called outside the scheduler for immediate updates.
     */
    public static void processPlayerManual(SkyBlockPlayer player) {
        processPlayer(player);
    }

    /**
     * Gets the chocolate that would be produced in a given time period.
     * Useful for displaying "offline earnings" or projections.
     *
     * @param player The player
     * @param seconds The number of seconds
     * @return The amount of chocolate that would be produced
     */
    public static long getChocolateForTime(SkyBlockPlayer player, long seconds) {
        double cps = ChocolateCalculator.calculateTotalCps(player);
        return (long) (cps * seconds);
    }

    /**
     * Checks if the production task is running.
     */
    public static boolean isRunning() {
        return initialized;
    }
}
