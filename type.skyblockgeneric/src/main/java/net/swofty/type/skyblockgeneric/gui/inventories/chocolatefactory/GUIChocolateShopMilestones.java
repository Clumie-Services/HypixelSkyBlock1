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
import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateCalculator;
import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateFactoryData;
import net.swofty.type.skyblockgeneric.chocolatefactory.milestone.ShopMilestone;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop Milestones GUI - Shows all 24 shop milestones
 */
public class GUIChocolateShopMilestones extends HypixelInventoryGUI {

    public GUIChocolateShopMilestones() {
        super("Shop Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE, " "));
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        ChocolateFactoryData data = player.getChocolateFactoryData();

        // Back button
        set(new GUIClickableItem(49) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIChocolateShop().open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Chocolate Shop");
            }
        });

        // Info item
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                int reached = ShopMilestone.getMilestonesReached(factoryData.getShopSpent());
                double discount = ShopMilestone.getTotalDiscount(factoryData.getShopSpent());

                List<String> lore = new ArrayList<>();
                lore.add("§7Spend chocolate in the shop to");
                lore.add("§7unlock special rewards!");
                lore.add("");
                lore.add("§7Shop Spent: §6" + StringUtility.commaify(factoryData.getShopSpent()));
                lore.add("§7Milestones Reached: §a" + reached + "§7/§a" + ShopMilestone.values().length);
                lore.add("§7Total Discount: §a" + (int)(discount * 100) + "%");

                return ItemStackCreator.getStack("§6Shop Milestones", Material.GOLD_INGOT, 1, lore.toArray(new String[0]));
            }
        });

        // Display milestones (slots 10-16, 19-25, 28-34, 37-40)
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40
        };

        ShopMilestone[] milestones = ShopMilestone.values();
        for (int i = 0; i < Math.min(slots.length, milestones.length); i++) {
            ShopMilestone milestone = milestones[i];
            int slot = slots[i];

            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                    ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                    long shopSpent = factoryData.getShopSpent();
                    boolean reached = milestone.isReached(shopSpent);
                    double progress = milestone.getProgress(shopSpent);

                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    lore.add("§7Requirement: §6" + ChocolateCalculator.formatNumber(milestone.getChocolateRequired()) + " spent");
                    lore.add("");
                    lore.add("§7Reward:");
                    lore.add(milestone.getReward());
                    lore.add("");

                    if (reached) {
                        lore.add("§a§lCOMPLETED!");
                    } else {
                        lore.add("§7Progress: §e" + String.format("%.1f%%", progress));
                        lore.add(createProgressBar(progress));
                    }

                    Material material = reached ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                    String name = (reached ? "§a" : "§c") + "Milestone " + milestone.getNumber() + ": " + milestone.getName();

                    return ItemStackCreator.getStack(name, material, milestone.getNumber(), lore.toArray(new String[0]));
                }
            });
        }
    }

    private String createProgressBar(double percentage) {
        StringBuilder bar = new StringBuilder("§8[");
        int filled = (int) (percentage / 5);
        for (int i = 0; i < 20; i++) {
            bar.append(i < filled ? "§a=" : "§7=");
        }
        bar.append("§8]");
        return bar.toString();
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
