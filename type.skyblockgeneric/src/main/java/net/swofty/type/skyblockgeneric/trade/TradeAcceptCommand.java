package net.swofty.type.skyblockgeneric.trade;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

/**
 * Internal command to accept a trade request.
 * Usage: /tradeaccept <player name>
 */
@CommandParameters(
        aliases = "tacept",
        description = "Accept a trade request",
        usage = "/tradeaccept <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false
)
public class TradeAcceptCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString playerArg = ArgumentType.String("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String senderName = context.get(playerArg);

            handleAccept(player, senderName);
        }, playerArg);

        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;
            sender.sendMessage("§cUsage: /tradeaccept <player>");
        });
    }

    private void handleAccept(SkyBlockPlayer accepter, String senderName) {
        // Find sender UUID
        UUID senderUUID = SkyBlockDataHandler.getPotentialUUIDFromName(senderName);
        if (senderUUID == null) {
            accepter.sendMessage("§cCouldn't find a player with that name!");
            return;
        }

        // Check if there's a pending request
        var request = TradeSessionManager.getInstance().getRequest(senderUUID, accepter.getUuid());
        if (request.isEmpty()) {
            accepter.sendMessage("§cYou don't have a pending trade request from that player!");
            return;
        }

        // Check if request expired
        if (request.get().isExpired()) {
            accepter.sendMessage("§cThat trade request has expired!");
            TradeSessionManager.getInstance().denyRequest(accepter.getUuid(), senderUUID);
            return;
        }

        // Check if accepter is already in a trade
        if (TradeSessionManager.getInstance().isInTrade(accepter.getUuid())) {
            accepter.sendMessage("§cYou are already in a trade!");
            return;
        }

        // Find sender online
        SkyBlockPlayer sender = findOnlinePlayer(senderUUID);
        if (sender == null) {
            accepter.sendMessage("§cThat player is no longer online!");
            TradeSessionManager.getInstance().denyRequest(accepter.getUuid(), senderUUID);
            return;
        }

        // Check if sender is already in a trade
        if (TradeSessionManager.getInstance().isInTrade(senderUUID)) {
            accepter.sendMessage("§cThat player is already in another trade!");
            TradeSessionManager.getInstance().denyRequest(accepter.getUuid(), senderUUID);
            return;
        }

        // Accept the trade
        if (TradeSessionManager.getInstance().acceptRequest(sender, accepter)) {
            accepter.sendMessage("§aYou accepted the trade request!");
            sender.sendMessage("§a" + accepter.getUsername() + " accepted your trade request!");
        } else {
            accepter.sendMessage("§cCould not start the trade. Please try again.");
        }
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
