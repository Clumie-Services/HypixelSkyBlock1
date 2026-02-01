package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.entity.mob.MobRegistry;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.stream.Collectors;

@CommandParameters(aliases = "sm",
        description = "Spawns a mob at the player's location",
        usage = "/spawnmob <mob> [amount]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SpawnMobCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString mobArgument = ArgumentType.String("mob");
        ArgumentInteger amountArgument = ArgumentType.Integer("amount");

        // Show available mobs when no argument is provided
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;

            List<String> mobNames = MobRegistry.REGISTERED_MOBS.stream()
                    .map(registry -> registry.getClazz().getSimpleName())
                    .sorted()
                    .collect(Collectors.toList());

            sender.sendMessage("§eAvailable mobs (" + mobNames.size() + "):");
            sender.sendMessage("§7" + String.join(", ", mobNames));
        });

        // Spawn single mob
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String mobName = context.get(mobArgument);
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            MobRegistry registry = findMobRegistry(mobName);
            if (registry == null) {
                sender.sendMessage("§cMob not found: §e" + mobName);
                sender.sendMessage("§7Use /spawnmob to see available mobs.");
                return;
            }

            spawnMob(player, registry, 1);
        }, mobArgument);

        // Spawn multiple mobs
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String mobName = context.get(mobArgument);
            int amount = context.get(amountArgument);
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            if (amount < 1 || amount > 100) {
                sender.sendMessage("§cAmount must be between 1 and 100.");
                return;
            }

            MobRegistry registry = findMobRegistry(mobName);
            if (registry == null) {
                sender.sendMessage("§cMob not found: §e" + mobName);
                sender.sendMessage("§7Use /spawnmob to see available mobs.");
                return;
            }

            spawnMob(player, registry, amount);
        }, mobArgument, amountArgument);
    }

    private MobRegistry findMobRegistry(String mobName) {
        String searchName = mobName.toLowerCase().replace("_", "");

        for (MobRegistry registry : MobRegistry.REGISTERED_MOBS) {
            String className = registry.getClazz().getSimpleName().toLowerCase();
            if (className.equals(searchName) || className.equals("mob" + searchName)) {
                return registry;
            }
        }

        // Partial match
        for (MobRegistry registry : MobRegistry.REGISTERED_MOBS) {
            String className = registry.getClazz().getSimpleName().toLowerCase();
            if (className.contains(searchName)) {
                return registry;
            }
        }

        return null;
    }

    private void spawnMob(SkyBlockPlayer player, MobRegistry registry, int amount) {
        for (int i = 0; i < amount; i++) {
            SkyBlockMob mob = registry.asMob();
            if (mob == null) {
                player.sendMessage("§cFailed to spawn mob.");
                return;
            }

            double offsetX = (Math.random() - 0.5) * 2;
            double offsetZ = (Math.random() - 0.5) * 2;

            mob.setInstance(
                    player.getInstance(),
                    player.getPosition().add(offsetX, 0, offsetZ)
            );
        }

        String mobName = registry.getClazz().getSimpleName();
        if (amount == 1) {
            player.sendMessage("§aSpawned §e" + mobName + "§a.");
        } else {
            player.sendMessage("§aSpawned §e" + amount + "x " + mobName + "§a.");
        }
    }
}
