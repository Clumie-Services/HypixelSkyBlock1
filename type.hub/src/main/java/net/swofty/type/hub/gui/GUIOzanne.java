package net.swofty.type.hub.gui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIOzanne extends HypixelInventoryGUI {

    public GUIOzanne() {
        super("Ozanne", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(31));

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.PLAYER, 1.0f, 0f));
                p.sendMessage("§cThis feature is not yet implemented!");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStackHead(
                        "§aSell Reforges On Accessories",
                        "961a918c0c49ba8d053e522cb91abc74689367b4d8aa06bfc1ba9154730985ff",
                        1,
                        "§7Ozanne will remove and refund",
                        "§7reforge stones applied to your",
                        "§7accessories.",
                        "",
                        "§eClick to open!"
                );
            }
        });

        updateItemStacks(getInventory(), getPlayer());

        // Play click sound when menu opens
        getPlayer().playSound(Sound.sound(Key.key("random.click"), Sound.Source.PLAYER, 1.0f, 1.0f));
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
