package net.swofty.type.skyblockgeneric.event.actions.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.data.datapoints.DatapointToggles;

import java.util.concurrent.ThreadLocalRandom;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionItemDrop implements HypixelEventClass {

    @HypixelEvent(node = EventNodes.PLAYER, requireDataLoaded = true)
    public void run(ItemDropEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ItemStack droppedStack = event.getItemStack();

        // Always cancel to handle drops ourselves
        event.setCancelled(true);

        if (new SkyBlockItem(droppedStack).getAttributeHandler().getTypeAsString().toLowerCase().contains("menu")) {
            return;
        }

        if (player.getOpenInventory() != null) {
            return;
        }

        // Remove the dropped amount from the player's inventory
        ItemStack currentItem = player.getInventory().getItemStack(player.getHeldSlot());
        int remainingAmount = currentItem.amount() - droppedStack.amount();
        if (remainingAmount <= 0) {
            player.getInventory().setItemStack(player.getHeldSlot(), ItemStack.AIR);
        } else {
            player.getInventory().setItemStack(player.getHeldSlot(), currentItem.withAmount(remainingAmount));
        }

        boolean hideMessage = player.getToggles().get(DatapointToggles.Toggles.ToggleType.DISABLE_DROP_MESSAGES);

        if (!hideMessage) {
            player.sendMessage(Component.text("§e⚠ §aYour drops can't be seen by other players in §bSkyBlock§a!")
                    .hoverEvent(Component.text("§eClick here to disable the alert!"))
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/toggledropalert"))
            );
            player.sendMessage(Component.text("§aOnly you can pickup your dropped items!")
                    .hoverEvent(Component.text("§eClick here to disable the alert!"))
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/toggledropalert"))
            );
            player.sendMessage(Component.text("§eClick here to disable this alert forever!")
                    .hoverEvent(Component.text("§eClick here to disable the alert!"))
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/toggledropalert")));
        }

        DroppedItemEntityImpl droppedItem = new DroppedItemEntityImpl(new SkyBlockItem(droppedStack), player);
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        double rx = rand.nextDouble() * 0.5 - 0.25;
        double rz = rand.nextDouble() * 0.5 - 0.25;
        Pos pos = Pos.fromPoint(player.getPosition().add(rx, 1.3, rz));

        droppedItem.setVelocity(player.getPosition().direction()
                .mul(5)
                .add(0, 1.5, 0)
        );

        droppedItem.setInstance(player.getInstance(), pos);
    }
}
