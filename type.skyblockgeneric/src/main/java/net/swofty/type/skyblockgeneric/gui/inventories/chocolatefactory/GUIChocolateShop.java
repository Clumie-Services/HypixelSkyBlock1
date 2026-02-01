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
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateFactoryData;
import net.swofty.type.skyblockgeneric.chocolatefactory.milestone.ShopMilestone;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Chocolate Shop GUI - Purchase items with chocolate
 */
public class GUIChocolateShop extends HypixelInventoryGUI {

    public GUIChocolateShop() {
        super("Chocolate Shop", InventoryType.CHEST_6_ROW);
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
                new GUIChocolateFactory().open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Chocolate Factory");
            }
        });

        // Shop Milestones button
        set(new GUIClickableItem(53) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIChocolateShopMilestones().open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                int reached = ShopMilestone.getMilestonesReached(factoryData.getShopSpent());

                List<String> lore = new ArrayList<>();
                lore.add("§7View your shop milestones!");
                lore.add("");
                lore.add("§7Milestones: §a" + reached + "§7/§a" + ShopMilestone.values().length);
                lore.add("");
                lore.add("§eClick to view!");

                return ItemStackCreator.getStack("§6Shop Milestones", Material.GOLD_INGOT, 1, lore.toArray(new String[0]));
            }
        });

        // Chocolate balance display
        set(new GUIClickableItem(4) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                double discount = ShopMilestone.getTotalDiscount(factoryData.getShopSpent());

                List<String> lore = new ArrayList<>();
                lore.add("§7Your chocolate: §6" + StringUtility.commaify(factoryData.getChocolate()));
                lore.add("§7Shop spent: §6" + StringUtility.commaify(factoryData.getShopSpent()));
                if (discount > 0) {
                    lore.add("");
                    lore.add("§7Shop discount: §a" + (int)(discount * 100) + "%");
                }

                return ItemStackCreator.getStack("§6Your Chocolate", Material.COCOA_BEANS, 1, lore.toArray(new String[0]));
            }
        });

        // Shop items
        createShopItem(player, 19, "§aRabbit Foot Charm", Material.RABBIT_FOOT, 500_000,
                "§7Increases your luck when", "§7finding rabbits!");

        createShopItem(player, 20, "§9Golden Carrot Bundle", Material.GOLDEN_CARROT, 1_000_000,
                "§7A bundle of golden carrots", "§7for your rabbits!");

        createShopItem(player, 21, "§5Chocolate Bar", Material.GOLD_INGOT, 5_000_000,
                "§7A premium chocolate bar", "§7with extra bonuses!");

        createShopItem(player, 22, "§6Rabbit Statue", Material.ARMOR_STAND, 10_000_000,
                "§7A decorative rabbit statue", "§7for your island!");

        createShopItem(player, 23, "§dHoppity Plushie", Material.PLAYER_HEAD, 25_000_000,
                "§7A cute plushie of Hoppity!");

        createShopItem(player, 24, "§cChocolate Factory Blueprint", Material.PAPER, 100_000_000,
                "§7Unlock a special factory", "§7upgrade!");

        createShopItem(player, 25, "§bTime Warp Token", Material.CLOCK, 50_000_000,
                "§7Instantly recharge one", "§7Time Tower charge!");
    }

    private void createShopItem(SkyBlockPlayer player, int slot, String name, Material material, long baseCost, String... description) {
        ChocolateFactoryData data = player.getChocolateFactoryData();
        double discount = ShopMilestone.getTotalDiscount(data.getShopSpent());
        long finalCost = (long) (baseCost * (1 - discount));

        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                double currentDiscount = ShopMilestone.getTotalDiscount(factoryData.getShopSpent());
                long cost = (long) (baseCost * (1 - currentDiscount));

                if (factoryData.spendAtShop(cost)) {
                    sbPlayer.getSkyblockDataHandler()
                            .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                            .setValue(factoryData);

                    sbPlayer.sendMessage("§aPurchased " + name + "§a!");
                } else {
                    sbPlayer.sendMessage("§cYou don't have enough chocolate!");
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                double currentDiscount = ShopMilestone.getTotalDiscount(factoryData.getShopSpent());
                long cost = (long) (baseCost * (1 - currentDiscount));

                List<String> lore = new ArrayList<>();
                for (String desc : description) {
                    lore.add(desc);
                }
                lore.add("");
                if (currentDiscount > 0) {
                    lore.add("§7Cost: §6" + StringUtility.commaify(cost) + " §8(was " + StringUtility.commaify(baseCost) + ")");
                } else {
                    lore.add("§7Cost: §6" + StringUtility.commaify(cost) + " Chocolate");
                }
                lore.add("");
                if (factoryData.getChocolate() >= cost) {
                    lore.add("§eClick to purchase!");
                } else {
                    lore.add("§cNot enough chocolate!");
                }

                return ItemStackCreator.getStack(name, material, 1, lore.toArray(new String[0]));
            }
        });
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
