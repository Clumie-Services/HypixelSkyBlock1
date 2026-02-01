package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.kyori.adventure.text.Component;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandParameters(aliases = "regions",
        description = "Handles regions across the server",
        usage = "/region <wand|pos1|pos2|create|remove|info|list|types|reload|clear>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class RegionCommand extends HypixelCommand {

    // Store player selections
    private static final Map<UUID, Pos> position1Selections = new HashMap<>();
    private static final Map<UUID, Pos> position2Selections = new HashMap<>();

    // Tag for region wand
    public static final Tag<Boolean> REGION_WAND_TAG = Tag.Boolean("region_wand").defaultValue(false);

    /**
     * Creates the region selection wand item.
     */
    public static ItemStack createWand() {
        return ItemStack.builder(Material.GOLDEN_AXE)
                .customName(Component.text("§6§lRegion Wand"))
                .lore(
                        Component.text("§7Left-click a block to set §ePosition 1"),
                        Component.text("§7Right-click a block to set §ePosition 2"),
                        Component.text(""),
                        Component.text("§eUse /region create to finalize")
                )
                .set(REGION_WAND_TAG, true)
                .build();
    }

    /**
     * Checks if an item is the region wand.
     */
    public static boolean isWand(ItemStack item) {
        return item != null && item.getTag(REGION_WAND_TAG);
    }

    /**
     * Sets position 1 for a player.
     */
    public static void setPosition1(UUID playerUuid, Pos pos) {
        position1Selections.put(playerUuid, pos);
    }

    /**
     * Sets position 2 for a player.
     */
    public static void setPosition2(UUID playerUuid, Pos pos) {
        position2Selections.put(playerUuid, pos);
    }

    /**
     * Gets position 1 for a player.
     */
    public static Pos getPosition1(UUID playerUuid) {
        return position1Selections.get(playerUuid);
    }

    /**
     * Gets position 2 for a player.
     */
    public static Pos getPosition2(UUID playerUuid) {
        return position2Selections.get(playerUuid);
    }

    /**
     * Clears selections for a player.
     */
    public static void clearSelections(UUID playerUuid) {
        position1Selections.remove(playerUuid);
        position2Selections.remove(playerUuid);
    }

    @Override
    public void registerUsage(MinestomCommand command) {
        // /region wand - Get the region selection wand
        ArgumentLiteral wandArg = ArgumentType.Literal("wand");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            player.getInventory().addItemStack(createWand());
            player.sendMessage("§aYou have been given the §6Region Wand§a!");
            player.sendMessage("§7Left-click a block to set §ePosition 1");
            player.sendMessage("§7Right-click a block to set §ePosition 2");
        }, wandArg);

        // /region pos1 - Set position 1 to current location
        ArgumentLiteral pos1Arg = ArgumentType.Literal("pos1");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            Pos pos = player.getPosition().withYaw(0).withPitch(0);
            Pos blockPos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
            setPosition1(player.getUuid(), blockPos);

            player.sendMessage("§aPosition 1 set to §e(" + blockPos.blockX() + ", " + blockPos.blockY() + ", " + blockPos.blockZ() + ")");
            showSelectionInfo(player);
        }, pos1Arg);

        // /region pos2 - Set position 2 to current location
        ArgumentLiteral pos2Arg = ArgumentType.Literal("pos2");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            Pos pos = player.getPosition().withYaw(0).withPitch(0);
            Pos blockPos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
            setPosition2(player.getUuid(), blockPos);

            player.sendMessage("§aPosition 2 set to §e(" + blockPos.blockX() + ", " + blockPos.blockY() + ", " + blockPos.blockZ() + ")");
            showSelectionInfo(player);
        }, pos2Arg);

        // /region create <id> <type> - Create a region with current selection
        ArgumentLiteral createArg = ArgumentType.Literal("create");
        ArgumentString regionIdArg = ArgumentType.String("region_id");
        ArgumentEnum<RegionType> regionTypeArg = ArgumentType.Enum("region_type", RegionType.class);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            Pos pos1 = getPosition1(player.getUuid());
            Pos pos2 = getPosition2(player.getUuid());

            if (pos1 == null || pos2 == null) {
                player.sendMessage("§cYou must set both positions first!");
                player.sendMessage("§7Use §e/region pos1 §7and §e/region pos2 §7or the §6Region Wand§7.");
                return;
            }

            String regionId = context.get(regionIdArg);
            RegionType regionType = context.get(regionTypeArg);

            // Check if region already exists
            if (SkyBlockRegion.getFromID(regionId) != null) {
                player.sendMessage("§cA region with ID §e" + regionId + " §calready exists!");
                player.sendMessage("§7Use §e/region remove " + regionId + " §7to delete it first.");
                return;
            }

            SkyBlockRegion region = new SkyBlockRegion(
                    regionId,
                    pos1,
                    pos2,
                    regionType,
                    HypixelConst.getTypeLoader().getType()
            );
            region.save();

            player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("§a§lREGION CREATED!");
            player.sendMessage("§7ID: §e" + regionId);
            player.sendMessage("§7Type: §e" + regionType.getName() + " §7(" + regionType.name() + ")");
            player.sendMessage("§7Position 1: §e(" + pos1.blockX() + ", " + pos1.blockY() + ", " + pos1.blockZ() + ")");
            player.sendMessage("§7Position 2: §e(" + pos2.blockX() + ", " + pos2.blockY() + ", " + pos2.blockZ() + ")");
            player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

            // Clear selection after creating
            clearSelections(player.getUuid());
        }, createArg, regionIdArg, regionTypeArg);

        // /region remove <id> - Remove a region
        ArgumentLiteral removeArg = ArgumentType.Literal("remove");
        ArgumentString removeIdArg = ArgumentType.String("region_id");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            String regionId = context.get(removeIdArg);
            SkyBlockRegion region = SkyBlockRegion.getFromID(regionId);

            if (region == null) {
                player.sendMessage("§cUnable to find a region with ID §e" + regionId + "§c.");
                return;
            }

            region.delete();
            player.sendMessage("§aSuccessfully deleted region §e" + regionId + "§a.");
        }, removeArg, removeIdArg);

        // /region info - Show info about current location's region or selection
        ArgumentLiteral infoArg = ArgumentType.Literal("info");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            // Show current selection
            Pos pos1 = getPosition1(player.getUuid());
            Pos pos2 = getPosition2(player.getUuid());

            player.sendMessage("§6=== Region Info ===");

            if (pos1 != null || pos2 != null) {
                player.sendMessage("§7Current Selection:");
                if (pos1 != null) {
                    player.sendMessage("  §7Position 1: §e(" + pos1.blockX() + ", " + pos1.blockY() + ", " + pos1.blockZ() + ")");
                } else {
                    player.sendMessage("  §7Position 1: §cnot set");
                }
                if (pos2 != null) {
                    player.sendMessage("  §7Position 2: §e(" + pos2.blockX() + ", " + pos2.blockY() + ", " + pos2.blockZ() + ")");
                } else {
                    player.sendMessage("  §7Position 2: §cnot set");
                }
                if (pos1 != null && pos2 != null) {
                    int volume = Math.abs(pos2.blockX() - pos1.blockX() + 1) *
                                 Math.abs(pos2.blockY() - pos1.blockY() + 1) *
                                 Math.abs(pos2.blockZ() - pos1.blockZ() + 1);
                    player.sendMessage("  §7Volume: §e" + volume + " blocks");
                }
                player.sendMessage("");
            }

            // Show region at current location
            SkyBlockRegion currentRegion = SkyBlockRegion.getRegionOfPosition(player.getPosition());
            if (currentRegion != null) {
                player.sendMessage("§7Region at your location:");
                player.sendMessage("  §7ID: §e" + currentRegion.getName());
                player.sendMessage("  §7Type: §e" + currentRegion.getType().getName() + " §7(" + currentRegion.getType().name() + ")");
                player.sendMessage("  §7Bounds: §e(" + currentRegion.getFirstLocation().blockX() + ", " + currentRegion.getFirstLocation().blockY() + ", " + currentRegion.getFirstLocation().blockZ() + ")");
                player.sendMessage("       §eto §e(" + currentRegion.getSecondLocation().blockX() + ", " + currentRegion.getSecondLocation().blockY() + ", " + currentRegion.getSecondLocation().blockZ() + ")");
            } else {
                player.sendMessage("§7You are not in any region.");
            }
        }, infoArg);

        // /region list - List all regions
        ArgumentLiteral listArg = ArgumentType.Literal("list");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            var regions = SkyBlockRegion.getRegions();
            player.sendMessage("§6=== Cached Regions (" + regions.size() + ") ===");

            for (SkyBlockRegion region : regions) {
                String color = region.getType().getColor();
                var bounds = region.getBounds();
                player.sendMessage(color + region.getName() + " §7- " + region.getType().getName() +
                    " §8[" + bounds.get(0) + "," + bounds.get(2) + "," + bounds.get(4) +
                    " to " + bounds.get(1) + "," + bounds.get(3) + "," + bounds.get(5) + "]");
            }

            if (regions.isEmpty()) {
                player.sendMessage("§7No regions cached for this server type.");
                player.sendMessage("§7Use §e/region create <id> <type> §7to create one.");
            }
        }, listArg);

        // /region types - List all available region types
        ArgumentLiteral typesArg = ArgumentType.Literal("types");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            player.sendMessage("§6=== Available Region Types ===");

            // Group by category based on name patterns
            StringBuilder hubTypes = new StringBuilder("§e§lHub: §r");
            StringBuilder mineTypes = new StringBuilder("§7§lMines: §r");
            StringBuilder forestTypes = new StringBuilder("§a§lForest: §r");
            StringBuilder combatTypes = new StringBuilder("§c§lCombat: §r");
            StringBuilder otherTypes = new StringBuilder("§b§lOther: §r");

            for (RegionType type : RegionType.values()) {
                String name = type.name();
                String display = type.getColor() + type.getName() + "§7, ";

                if (name.contains("MINE") || name.contains("CAVERN") || name.contains("DWARVEN") || name.contains("QUARRY")) {
                    mineTypes.append(display);
                } else if (name.contains("FOREST") || name.contains("PARK") || name.contains("WOOD") || name.contains("THICKET") || name.contains("JUNGLE")) {
                    forestTypes.append(display);
                } else if (name.contains("VILLAGE") || name.contains("BANK") || name.contains("AUCTION") || name.contains("BAZAAR") || name.contains("LIBRARY") || name.contains("HUB")) {
                    hubTypes.append(display);
                } else if (name.contains("SPIDER") || name.contains("GRAVEYARD") || name.contains("END") || name.contains("BLAZING") || name.contains("CRYPT")) {
                    combatTypes.append(display);
                } else {
                    otherTypes.append(display);
                }
            }

            player.sendMessage(hubTypes.toString());
            player.sendMessage(mineTypes.toString());
            player.sendMessage(forestTypes.toString());
            player.sendMessage(combatTypes.toString());
            player.sendMessage(otherTypes.toString());
            player.sendMessage("");
            player.sendMessage("§7Total: §e" + RegionType.values().length + " §7types available");
        }, typesArg);

        // /region reload - Reload regions from database
        ArgumentLiteral reloadArg = ArgumentType.Literal("reload");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            player.sendMessage("§7Reloading regions from database...");
            SkyBlockRegion.cacheRegions();
            player.sendMessage("§aReloaded §e" + SkyBlockRegion.getRegions().size() + " §aregions.");
        }, reloadArg);

        // /region clear - Clear current selection
        ArgumentLiteral clearArg = ArgumentType.Literal("clear");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            clearSelections(player.getUuid());
            player.sendMessage("§aCleared region selection.");
        }, clearArg);
    }

    private void showSelectionInfo(SkyBlockPlayer player) {
        Pos pos1 = getPosition1(player.getUuid());
        Pos pos2 = getPosition2(player.getUuid());

        if (pos1 != null && pos2 != null) {
            int volume = Math.abs(pos2.blockX() - pos1.blockX() + 1) *
                         Math.abs(pos2.blockY() - pos1.blockY() + 1) *
                         Math.abs(pos2.blockZ() - pos1.blockZ() + 1);
            player.sendMessage("§7Selection complete! Volume: §e" + volume + " blocks");
            player.sendMessage("§7Use §e/region create <id> <type> §7to create the region.");
        }
    }
}
