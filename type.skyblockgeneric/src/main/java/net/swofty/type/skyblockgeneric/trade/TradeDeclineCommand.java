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
 * Internal command to decline a trade request.
 * Usage: /tradedecline <player name>
 */
@CommandParameters(
        aliases = "tdecline",
        description = "Decline a trade request",
        usage = "/tradedecline <player>",
        permission = Rank.DEFAULT,
        allowsConsole = false
)
public class TradeDeclineCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString playerArg = ArgumentType.String("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String senderName = context.get(playerArg);

            handleDecline(player, senderName);
        }, playerArg);

        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;
            sender.sendMessage("§cUsage: /tradedecline <player>");
        });
    }

    private void handleDecline(SkyBlockPlayer decliner, String senderName) {
        // Find sender UUID
        UUID senderUUID = SkyBlockDataHandler.getPotentialUUIDFromName(senderName);
        if (senderUUID == null) {
            decliner.sendMessage("§cCouldn't find a player with that name!");
            return;
        }

        // Check if there's a pending request
        var request = TradeSessionManager.getInstance().getRequest(senderUUID, decliner.getUuid());
        if (request.isEmpty()) {
            decliner.sendMessage("§cYou don't have a pending trade request from that player!");
            return;
        }

        // Decline the request
        if (TradeSessionManager.getInstance().denyRequest(decliner.getUuid(), senderUUID)) {
            decliner.sendMessage("§cYou declined the trade request.");

            // Notify sender if online
            SkyBlockPlayer sender = findOnlinePlayer(senderUUID);
            if (sender != null) {
                sender.sendMessage("§c" + decliner.getUsername() + " declined your trade request.");
            }
        } else {
            decliner.sendMessage("§cCould not decline the trade request.");
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
