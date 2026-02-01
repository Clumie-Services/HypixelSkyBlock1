package net.swofty.type.skyblockgeneric.item.handlers.interactable;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.GUISkyBlockMenu;
import net.swofty.type.skyblockgeneric.user.SkyBlockIsland;
import net.swofty.type.skyblockgeneric.utility.JerryInformation;

import java.util.HashMap;
import java.util.Map;

public class InteractableRegistry {
    private static final Map<String, InteractableItemConfig> REGISTERED_HANDLERS = new HashMap<>();

    static {
        register("SKYBLOCK_MENU_INTERACT", InteractableItemConfig.builder().rightClickHandler(
                ((skyBlockPlayer, skyBlockItem) -> skyBlockPlayer.openView(new GUISkyBlockMenu()))
        ).leftClickHandler(
                ((skyBlockPlayer, skyBlockItem) -> skyBlockPlayer.openView(new GUISkyBlockMenu()))
        ).inventoryInteractHandler(
                ((skyBlockPlayer, skyBlockItem) -> {
                    skyBlockPlayer.openView(new GUISkyBlockMenu());
                    return false;
                })
        ).build());
        register("MOVE_JERRY_INTERACT", InteractableItemConfig.builder().rightClickHandler(
                ((player, skyBlockItem) -> {
                    if (!HypixelConst.isIslandServer()) {
                        player.sendMessage("§cYou can't move Jerry here! He doesn't belong here!");
                        return;
                    }

                    Point position = player.getTargetBlockPosition(5);
                    if (position == null) {
                        player.sendMessage("§cWhy would you try :(");
                        return;
                    }

                    // Check if position is in the void (too low)
                    if (position.y() < 1) {
                        player.sendMessage("§cWhy would you try :(");
                        return;
                    }

                    // Move up one block and center jerry
                    position = position.add(0.5, 1, 0.5);

                    // Verify there are 2 blocks of air available for Jerry to stand
                    Point finalPosition = position;
                    if (!player.getInstance().getBlock(position).isAir() ||
                            !player.getInstance().getBlock(position.add(0, 1, 0)).isAir()) {
                        // Find a safe spot with 2 blocks of air by searching upward
                        Point searchPos = position;
                        boolean foundSafeSpot = false;
                        for (int y = 0; y < 10; y++) {
                            Point checkPos = searchPos.add(0, y, 0);
                            if (player.getInstance().getBlock(checkPos).isAir() &&
                                    player.getInstance().getBlock(checkPos.add(0, 1, 0)).isAir()) {
                                finalPosition = checkPos;
                                foundSafeSpot = true;
                                break;
                            }
                        }
                        if (!foundSafeSpot) {
                            player.sendMessage("§cCouldn't find a safe spot for Jerry!");
                            return;
                        }
                    }

                    SkyBlockIsland island = player.getSkyBlockIsland();
                    JerryInformation jerryInformation = island.getJerryInformation();

                    ServerHolograms.ExternalHologram hologram = jerryInformation.getHologram();
                    ServerHolograms.removeExternalHologram(hologram);

                    jerryInformation.setJerryPosition(new Pos(finalPosition).withLookAt(player.getPosition()));
                    jerryInformation.getJerry().teleport(jerryInformation.getJerryPosition());
                    jerryInformation.getJerry().lookAt(player.getPosition().add(0, 1.4, 0));

                    hologram = ServerHolograms.ExternalHologram.builder()
                            .text(hologram.getText())
                            .instance(hologram.getInstance())
                            .pos(jerryInformation.getJerryPosition().add(0, 1, 0))
                            .build();

                    ServerHolograms.addExternalHologram(hologram);
                    jerryInformation.setHologram(hologram);
                    skyBlockItem.setAmount(0);
                })
        ).build());
    }

    public static void register(String id, InteractableItemConfig handler) {
        REGISTERED_HANDLERS.put(id, handler);
    }

    public static InteractableItemConfig getHandler(String id) {
        return REGISTERED_HANDLERS.get(id);
    }
}