package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIBazaarSettings extends HypixelInventoryGUI {

    public GUIBazaarSettings() {
        super("Bazaar \u279C Settings", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));

        SkyBlockPlayer player = (SkyBlockPlayer) e.player();

        // Slot 11: Instasell Ignore List
        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                // Currently just informational - toggle is on individual product menus
                p.sendMessage("§7Find the instasell ignore toggle on each individual product menu.");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                List<String> ignoreList = sbPlayer.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.BAZAAR_INSTASELL_IGNORE, DatapointStringList.class)
                        .getValue();

                List<String> lore = new ArrayList<>();
                lore.add("§7Manage products which are ignored");
                lore.add("§7from bulk instasells.");
                lore.add(" ");

                if (ignoreList == null || ignoreList.isEmpty() || (ignoreList.size() == 1 && ignoreList.get(0).isEmpty())) {
                    lore.add("§7Ignored: §aNone!");
                } else {
                    lore.add("§7Ignored: §e" + ignoreList.size() + " products");
                }

                lore.add(" ");
                lore.add("§8Find the instasell ignore toggle on");
                lore.add("§8each individual product menu.");

                return ItemStackCreator.getStack(
                        "§aInstasell Ignore List",
                        Material.DROPPER,
                        1,
                        lore
                );
            }
        });

        // Slot 15: Direct Mode Toggle
        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                boolean newValue = sbPlayer.getToggles().inverse(DatapointToggles.Toggles.ToggleType.BAZAAR_DIRECT_MODE);

                if (newValue) {
                    p.sendMessage("§aDirect Mode §7is now §aenabled§7!");
                } else {
                    p.sendMessage("§aDirect Mode §7is now §cdisabled§7!");
                }

                // Refresh the GUI
                new GUIBazaarSettings().open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                boolean directMode = sbPlayer.getToggles().get(DatapointToggles.Toggles.ToggleType.BAZAAR_DIRECT_MODE);

                List<String> lore = new ArrayList<>();
                lore.add("§8Bazaar View");
                lore.add(" ");
                lore.add("§7View buy and sell prices of each");
                lore.add("§7product.");
                lore.add(" ");
                lore.add("§7Status: " + (directMode ? "§aEnabled" : "§cDisabled"));
                lore.add(" ");
                lore.add("§eClick to toggle view!");

                return ItemStackCreator.getStack(
                        "§aDirect Mode",
                        Material.IRON_ORE,
                        1,
                        lore
                );
            }
        });

        // Slot 31: Go Back
        set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIBazaar(BazaarCategories.FARMING).open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack(
                        "§aGo Back",
                        Material.ARROW,
                        1,
                        "§7To Bazaar"
                );
            }
        });

        updateItemStacks(getInventory(), player);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
