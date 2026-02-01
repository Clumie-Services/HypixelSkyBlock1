package net.swofty.type.skyblockgeneric.trade;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

/**
 * Command to initiate a trade with another player.
 * Usage: /trade <player name>
 */
@CommandParameters(
        aliases = "tr",
        description = "Start a trade with another player",
        usage = "/trade <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false
)
public class TradeCommand extends HypixelCommand {

    /**
     * Maximum distance between players to initiate a trade.
     */
    private static final double MAX_TRADE_DISTANCE = 30.0;

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString playerArg = ArgumentType.String("player");

        // /trade <player>
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String targetName = context.get(playerArg);

            handleTradeRequest(player, targetName);
        }, playerArg);

        // /trade (no args)
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;
            sender.sendMessage("§cMissing arguments! Usage: /trade <player name>");
        });
    }

    private void handleTradeRequest(SkyBlockPlayer sender, String targetName) {
        // Check self-trade
        if (targetName.equalsIgnoreCase(sender.getUsername())) {
            sender.sendMessage("§cYou cannot trade with yourself!");
            return;
        }

        // Check if sender is already in a trade
        if (TradeSessionManager.getInstance().isInTrade(sender.getUuid())) {
            sender.sendMessage("§cYou are already in a trade!");
            return;
        }

        // Find target player
        UUID targetUUID = SkyBlockDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cCouldn't find a player with that name!");
            return;
        }

        // Get online player
        SkyBlockPlayer target = findOnlinePlayer(targetUUID);
        if (target == null) {
            sender.sendMessage("§cThat player is not online!");
            return;
        }

        // Check if target is already in a trade
        if (TradeSessionManager.getInstance().isInTrade(targetUUID)) {
            sender.sendMessage("§cThat player is already in a trade!");
            return;
        }

        // Check distance
        if (sender.getInstance() != target.getInstance()) {
            sender.sendMessage("§cYou are too far away to trade with that player!");
            return;
        }

        double distance = sender.getPosition().distance(target.getPosition());
        if (distance > MAX_TRADE_DISTANCE) {
            sender.sendMessage("§cYou are too far away to trade with that player!");
            return;
        }

        // Check if there's already a pending request from target to sender
        var existingRequest = TradeSessionManager.getInstance().getRequest(targetUUID, sender.getUuid());
        if (existingRequest.isPresent()) {
            // Accept the existing request
            if (TradeSessionManager.getInstance().acceptRequest(target, sender)) {
                sender.sendMessage("§aYou accepted the trade request from " + target.getUsername() + "!");
                target.sendMessage("§a" + sender.getUsername() + " accepted your trade request!");
            } else {
                sender.sendMessage("§cCould not start the trade. Please try again.");
            }
            return;
        }

        // Check if sender already has a pending request to this target
        var existingOutgoing = TradeSessionManager.getInstance().getRequest(sender.getUuid(), targetUUID);
        if (existingOutgoing.isPresent()) {
            sender.sendMessage("§cYou already have a pending trade request to that player!");
            sender.sendMessage("§7Wait for them to accept or try again later.");
            return;
        }

        // Send new trade request
        if (!TradeSessionManager.getInstance().sendRequest(sender, targetUUID)) {
            sender.sendMessage("§cCould not send trade request. Please try again.");
            return;
        }

        // Notify sender
        sender.sendMessage("§aTrade request sent to " + target.getUsername() + "!");
        sender.sendMessage("§7They have 60 seconds to accept.");

        // Notify target with clickable message
        target.sendMessage("");
        target.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        target.sendMessage("§e" + sender.getUsername() + " §7wants to trade with you!");
        target.sendMessage("");

        // Create clickable accept message
        Component acceptButton = Component.text("§a§l[ACCEPT]")
                .hoverEvent(HoverEvent.showText(Component.text("§aClick to accept the trade")))
                .clickEvent(ClickEvent.runCommand("/tradeaccept " + sender.getUsername()));

        Component declineButton = Component.text("§c§l[DECLINE]")
                .hoverEvent(HoverEvent.showText(Component.text("§cClick to decline the trade")))
                .clickEvent(ClickEvent.runCommand("/tradedecline " + sender.getUsername()));

        target.sendMessage(Component.text("   ").append(acceptButton).append(Component.text("  ")).append(declineButton));
        target.sendMessage("");
        target.sendMessage("§7You have §e60 seconds §7to respond.");
        target.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        target.sendMessage("");
    }

    private SkyBlockPlayer findOnlinePlayer(UUID uuid) {
        for (var player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (player instanceof SkyBlockPlayer sbp && sbp.getUuid().equals(uuid)) {
                return sbp;
            }
        }
        return null;
    }
}
