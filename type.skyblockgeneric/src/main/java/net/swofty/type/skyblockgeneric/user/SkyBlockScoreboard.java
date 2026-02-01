package net.swofty.type.skyblockgeneric.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.auctions.DarkAuctionPhase;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.utility.BlockUtility;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockProgressMission;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SkyBlockScoreboard {
    private static final HypixelScoreboard scoreboard = new HypixelScoreboard();
    private static Integer skyblockName = 0;

    public static void start() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        org.tinylog.Logger.info("SkyBlockScoreboard starting...");

        // Scoreboard Updater
        scheduler.submitTask(() -> {
            skyblockName++;
            if (skyblockName > 50) {
                skyblockName = 0;
            }

            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                try {
                    SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
                    SkyBlockRegion region = player.getRegion();
                    MissionData missionData = player.getMissionData();

                    if (dataHandler == null) {
                        continue;
                    }

                    // Build current lines
                    List<String> lines = buildScoreboardLines(player, dataHandler, region, missionData);
                    String title = "  " + getSidebarName(skyblockName, false)
                            + (player.isCoop() ? " §b§lCO-OP  " : "  ");

                    if (!scoreboard.hasScoreboard(player)) {
                        org.tinylog.Logger.info("Creating new scoreboard for player: " + player.getUsername());
                        scoreboard.createScoreboard(player, title);
                    }

                    scoreboard.updateLines(player, lines);
                    scoreboard.updateTitle(player, title);
                } catch (Exception e) {
                    org.tinylog.Logger.error(e, "Failed to update scoreboard for player: " + player.getUsername());
                }
            }
            return TaskSchedule.tick(20);
        });
    }

    private static List<String> buildScoreboardLines(SkyBlockPlayer player, SkyBlockDataHandler dataHandler,
                                                      SkyBlockRegion region, MissionData missionData) {
        List<String> lines = new ArrayList<>();
        lines.add("§7" + new SimpleDateFormat("MM/dd/yy").format(new Date()) + " §8" + HypixelConst.getServerName());
        lines.add("§7 ");
        lines.add("§f " + SkyBlockCalendar.getMonthName() + " " + StringUtility.ntify(SkyBlockCalendar.getDay()));
        lines.add("§7 " + SkyBlockCalendar.getDisplay(SkyBlockCalendar.getElapsed()));
        if (region != null && region.getType() != null) {
            lines.add(" §7⏣ " + region.getType().getColor() + region.getType().getName());
        } else {
            // Default to showing the server type name if no region is found
            String serverName = HypixelConst.getTypeLoader().getType().skyblockName();
            if (serverName != null && !serverName.isEmpty()) {
                // Format the server name nicely (e.g., "HUB" -> "Hub", "THE_PARK" -> "The Park")
                String formattedName = formatServerName(serverName);
                lines.add(" §7⏣ §b" + formattedName);
            } else {
                lines.add(" §7⏣ §7None");
            }
        }
        lines.add("§7 ");
        lines.add("§fPurse: §6" + StringUtility.commaify(dataHandler.get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue()));
        Integer bits = dataHandler.get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class).getValue();
        if (bits != null && bits > 0) {
            lines.add("§fBits: §b" + StringUtility.commaify(bits));
        }

        // Dark Auction section
        if (DarkAuctionHandler.isPlayerInAuction(player.getUuid())
                && DarkAuctionHandler.getLocalState() != null
                && DarkAuctionHandler.getLocalState().getPhase() == DarkAuctionPhase.BIDDING
        ) {
            lines.add("§8 ");
            DarkAuctionHandler.DarkAuctionLocalState auctionState = DarkAuctionHandler.getLocalState();
            int timeRemaining = DarkAuctionHandler.getTimeLeft().get();

            lines.add("§fTime Left: §9" + timeRemaining + "s");
            lines.add("§fCurrent Item:");

            String currentItem = auctionState.getCurrentItemType();
            if (currentItem != null) {
                try {
                    ItemType itemType = ItemType.valueOf(currentItem);
                    SkyBlockItem item = new SkyBlockItem(itemType);
                    lines.add(" " + item.getDisplayName());
                } catch (Exception e) {
                    lines.add(" §f" + currentItem.replace("_", " "));
                }
            } else {
                lines.add(" §7Waiting...");
            }
        } else {
            // Get active missions - filter by region if in a region, otherwise get all
            List<MissionData.ActiveMission> activeMissions = region != null
                    ? missionData.getActiveMissions(region.getType())
                    : missionData.getActiveMissions();

            if (!activeMissions.isEmpty()) {
                lines.add("§7 ");
                MissionData.ActiveMission mission = activeMissions.getFirst();
                SkyBlockMission skyBlockMission = MissionData.getMissionClass(mission.getMissionID());

                if (skyBlockMission instanceof LocationAssociatedMission locationAssociatedMission) {
                    lines.add("§fObjective " + BlockUtility.getArrow(
                            player.getPosition(),
                            locationAssociatedMission.getLocation()
                    ));
                    lines.add("§e" + mission);
                } else {
                    lines.add("§fObjective");
                    lines.add("§e" + mission);
                }

                SkyBlockProgressMission progressMission = missionData.getAsProgressMission(mission.getMissionID());
                if (progressMission != null)
                    lines.add("§7 (§e" + mission.getMissionProgress() + "§7/§a" + progressMission.getMaxProgress() + "§7)");
            }
        }

        lines.add("§7 ");
        lines.add("§ewww.hypixel.net");

        return lines;
    }

    public static void removeCache(Player player) {
        scoreboard.removeScoreboard(player);
    }

    /**
     * Immediately updates the scoreboard for a specific player.
     * Call this when a player's data is first loaded to avoid waiting for the scheduler tick.
     */
    public static void updatePlayer(SkyBlockPlayer player) {
        try {
            SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
            SkyBlockRegion region = player.getRegion();
            MissionData missionData = player.getMissionData();

            if (dataHandler == null) {
                return;
            }

            List<String> lines = buildScoreboardLines(player, dataHandler, region, missionData);
            String title = "  " + getSidebarName(skyblockName, false)
                    + (player.isCoop() ? " §b§lCO-OP  " : "  ");

            if (!scoreboard.hasScoreboard(player)) {
                scoreboard.createScoreboard(player, title);
            }

            scoreboard.updateLines(player, lines);
            scoreboard.updateTitle(player, title);
        } catch (Exception e) {
            org.tinylog.Logger.error(e, "Failed to update scoreboard for player: " + player.getUsername());
        }
    }


    /**
     * Formats a server name like "HUB" or "THE_PARK" into "Hub" or "The Park"
     */
    private static String formatServerName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(" ");
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return result.toString();
    }

    private static String getSidebarName(int counter, boolean isGuest) {
        String baseText = "SKYBLOCK";
        String[] colors = {"§f§l", "§6§l", "§e§l"};
        String endColor = "§a§l";
        String endText = isGuest ? " GUEST" : "";

        if (counter > 0 && counter <= 8) {
            return colors[0] + baseText.substring(0, counter - 1) +
                    colors[1] + baseText.charAt(counter - 1) +
                    colors[2] + baseText.substring(counter) +
                    endColor + endText;
        } else if ((counter >= 9 && counter <= 19) ||
                (counter >= 25 && counter <= 29)) {
            return colors[0] + baseText + endColor + endText;
        } else {
            return colors[2] + baseText + endColor + endText;
        }
    }
}
