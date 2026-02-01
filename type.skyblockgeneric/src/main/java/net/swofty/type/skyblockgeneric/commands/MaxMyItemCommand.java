package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.item.ItemAttributeHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantableComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import net.swofty.type.generic.user.categories.Rank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CommandParameters(aliases = "maxitem",
        description = "Maxes out all enchantments on the item in hand",
        usage = "/maxmyitem",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MaxMyItemCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            SkyBlockItem item = PlayerItemOrigin.getFromCache(player.getUuid()).get(PlayerItemOrigin.MAIN_HAND);

            if (item == null || item.isNA()) {
                player.sendMessage("§cYou must be holding an item!");
                return;
            }

            player.updateItem(PlayerItemOrigin.MAIN_HAND, (heldItem) -> {
                ItemAttributeHandler handler = heldItem.getAttributeHandler();
                int enchantmentsAdded = 0;
                Set<EnchantmentType> addedEnchants = new HashSet<>();
                boolean hasUltimate = false;

                // Get the item groups this item belongs to
                List<EnchantItemGroups> itemGroups = new ArrayList<>();
                if (heldItem.hasComponent(EnchantableComponent.class)) {
                    itemGroups = heldItem.getComponent(EnchantableComponent.class).getEnchantItemGroups();
                }

                // If no enchantable component, try to determine from item type
                if (itemGroups.isEmpty()) {
                    // Default to SWORD for weapons without specific component
                    itemGroups = List.of(EnchantItemGroups.SWORD);
                }

                for (EnchantmentType type : EnchantmentType.values()) {
                    Ench ench = type.getEnch();
                    List<EnchantItemGroups> enchantGroups = ench.getGroups();

                    // Check if this enchantment can be applied to this item
                    boolean canApply = false;
                    for (EnchantItemGroups group : enchantGroups) {
                        if (itemGroups.contains(group)) {
                            canApply = true;
                            break;
                        }
                    }

                    if (!canApply) continue;

                    // Skip One For All as it removes other enchantments
                    if (type == EnchantmentType.ONE_FOR_ALL) continue;

                    // Only allow one ultimate enchantment
                    if (type.isUltimate()) {
                        if (hasUltimate) continue;
                        hasUltimate = true;
                    }

                    // Check for conflicting enchantments
                    if (ench instanceof ConflictingEnch conflicting) {
                        boolean hasConflict = false;
                        for (EnchantmentType conflictType : conflicting.getConflictingEnchantments()) {
                            if (addedEnchants.contains(conflictType)) {
                                hasConflict = true;
                                break;
                            }
                        }
                        if (hasConflict) continue;
                    }

                    // Get max level for this enchantment
                    int maxLevel = ench.getLevelsToApply(player).maximumLevel();

                    // Add the enchantment at max level
                    handler.addEnchantment(
                            SkyBlockEnchantment.builder()
                                    .level(maxLevel)
                                    .type(type)
                                    .build()
                    );
                    addedEnchants.add(type);
                    enchantmentsAdded++;
                }

                player.sendMessage("§aSuccessfully added §e" + enchantmentsAdded + "§a max level enchantments to your item!");
            });
        });
    }
}
