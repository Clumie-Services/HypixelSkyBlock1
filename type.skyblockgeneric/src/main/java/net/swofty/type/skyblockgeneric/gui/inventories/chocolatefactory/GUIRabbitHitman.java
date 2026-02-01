package net.swofty.type.skyblockgeneric.gui.inventories.chocolatefactory;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateFactoryData;
import net.swofty.type.skyblockgeneric.chocolatefactory.HoppityCollectionData;
import net.swofty.type.skyblockgeneric.chocolatefactory.rabbit.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Rabbit Hitman GUI - Hire the hitman to find specific rabbits
 */
public class GUIRabbitHitman extends HypixelInventoryGUI {
    private static final String HITMAN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmRhMjg3YTQyOGJhMjAzNDY0YTliNjM2MWY3MzQ4OTVhMTNjMmU2NmE5OWE2YThjMjY3NmE1YmU2YzZmNTQzOSJ9fX0=";

    public GUIRabbitHitman() {
        super("Rabbit Hitman", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE, " "));
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        ChocolateFactoryData data = player.getChocolateFactoryData();
        HoppityCollectionData collection = player.getHoppityCollectionData();

        // Back button
        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIChocolateFactory().open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Chocolate Factory");
            }
        });

        // Hitman info
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<String> lore = new ArrayList<>();
                lore.add("§7The Rabbit Hitman can help you");
                lore.add("§7find specific rabbits you're");
                lore.add("§7looking for!");
                lore.add("");
                lore.add("§7§o\"Need a specific rabbit? I can");
                lore.add("§7§otrack it down... for a price.\"");

                return ItemStackCreator.getStackHead("§cRabbit Hitman", HITMAN_TEXTURE, 1, lore.toArray(new String[0]));
            }
        });

        // Hitman slot 1
        set(createHitmanSlot(player, data, collection, 0, 11));

        // Hitman slot 2
        set(createHitmanSlot(player, data, collection, 1, 13));

        // Hitman slot 3
        set(createHitmanSlot(player, data, collection, 2, 15));
    }

    private GUIClickableItem createHitmanSlot(SkyBlockPlayer player, ChocolateFactoryData data, HoppityCollectionData collection, int slotIndex, int guiSlot) {
        return new GUIClickableItem(guiSlot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                if (slotIndex >= factoryData.getRabbitHitmanSlots()) {
                    // Unlock slot
                    long cost = getSlotUnlockCost(slotIndex);
                    if (factoryData.removeChocolate(cost)) {
                        factoryData.setRabbitHitmanSlots(factoryData.getRabbitHitmanSlots() + 1);
                        sbPlayer.sendMessage("§aUnlocked Hitman Slot " + (slotIndex + 1) + "!");
                    } else {
                        sbPlayer.sendMessage("§cYou don't have enough chocolate!");
                    }
                } else {
                    // Use slot - pick a random uncollected rabbit
                    List<ChocolateRabbit> uncollected = sbPlayer.getHoppityCollectionData().getUncollectedRabbitsSorted();
                    if (uncollected.isEmpty()) {
                        sbPlayer.sendMessage("§aYou've already collected all rabbits!");
                        return;
                    }

                    long cost = 10_000_000L; // Base cost for hitman hunt
                    if (!factoryData.removeChocolate(cost)) {
                        sbPlayer.sendMessage("§cYou need " + StringUtility.commaify(cost) + " chocolate to hire the hitman!");
                        return;
                    }

                    // Pick a random rabbit from uncollected
                    ChocolateRabbit target = uncollected.get(new java.util.Random().nextInt(uncollected.size()));
                    sbPlayer.sendMessage("§aThe Hitman found " + target.getFormattedName() + "§a for you!");

                    // Add to collection
                    HoppityCollectionData collectionData = sbPlayer.getHoppityCollectionData();
                    collectionData.addRabbit(target.getId());
                    sbPlayer.getSkyblockDataHandler()
                            .get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.HOPPITY_COLLECTION,
                                    net.swofty.type.skyblockgeneric.data.datapoints.DatapointHoppityCollection.class)
                            .setValue(collectionData);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                List<String> lore = new ArrayList<>();

                if (slotIndex >= factoryData.getRabbitHitmanSlots()) {
                    // Locked slot
                    long cost = getSlotUnlockCost(slotIndex);
                    lore.add("§cLocked!");
                    lore.add("");
                    lore.add("§7Unlock this slot to hire");
                    lore.add("§7another hitman simultaneously!");
                    lore.add("");
                    lore.add("§7Cost: §6" + StringUtility.commaify(cost) + " Chocolate");
                    lore.add("");
                    lore.add("§eClick to unlock!");

                    return ItemStackCreator.getStack("§cHitman Slot " + (slotIndex + 1), Material.GRAY_STAINED_GLASS_PANE, 1, lore.toArray(new String[0]));
                } else {
                    // Unlocked slot
                    long cost = 10_000_000L;
                    lore.add("§aUnlocked!");
                    lore.add("");
                    lore.add("§7Hire the hitman to find a");
                    lore.add("§7random uncollected rabbit!");
                    lore.add("");
                    lore.add("§7Cost: §6" + StringUtility.commaify(cost) + " Chocolate");
                    lore.add("");
                    lore.add("§eClick to hire!");

                    return ItemStackCreator.getStack("§aHitman Slot " + (slotIndex + 1), Material.LIME_STAINED_GLASS_PANE, 1, lore.toArray(new String[0]));
                }
            }
        };
    }

    private long getSlotUnlockCost(int slotIndex) {
        return switch (slotIndex) {
            case 0 -> 0; // First slot is free
            case 1 -> 50_000_000L;
            case 2 -> 200_000_000L;
            default -> Long.MAX_VALUE;
        };
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
