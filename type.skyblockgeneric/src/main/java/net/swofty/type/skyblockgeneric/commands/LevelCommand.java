package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.GUISkyBlockLevels;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(description = "Opens the SkyBlock Leveling menu",
        usage = "/level",
        permission = Rank.DEFAULT,
        aliases = "levels sblevels sblevel",
        allowsConsole = false)
public class LevelCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, context) -> {
            if (!permissionCheck(sender)) return;

            ((SkyBlockPlayer) sender).openView(new GUISkyBlockLevels());
        });
    }
}
