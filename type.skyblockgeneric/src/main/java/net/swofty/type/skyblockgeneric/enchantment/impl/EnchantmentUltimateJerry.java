package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentUltimateJerry implements Ench, EventBasedEnchant {

    public static final int[] DAMAGE_INCREASE = new int[]{1000, 2000, 3000, 4000, 5000};

    @Override
    public String getDescription(int level) {
        return "Increases the base damage of §aAspect of the Jerry§7 by §c" + DAMAGE_INCREASE[level - 1] + "%§7.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 250,
                2, 500,
                3, 1000,
                4, 2000,
                5, 4000
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null) return ItemStatistics.empty();

        SkyBlockItem mainHand = PlayerItemOrigin.getFromCache(causer.getUuid()).get(PlayerItemOrigin.MAIN_HAND);
        if (mainHand == null) return ItemStatistics.empty();

        ItemType itemType = mainHand.getAttributeHandler().getPotentialType();
        if (itemType == null) return ItemStatistics.empty();

        // Check if holding Aspect of the Jerry
        String itemName = itemType.name();
        if (!itemName.contains("ASPECT_OF_THE_JERRY") && !itemName.contains("JERRY")) {
            return ItemStatistics.empty();
        }

        // Get the base damage of the weapon
        double baseDamage = mainHand.getAttributeHandler().getStatistics().getBase(ItemStatistic.DAMAGE);
        double bonusDamage = baseDamage * (DAMAGE_INCREASE[level - 1] / 100.0);

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, bonusDamage).build();
    }
}
