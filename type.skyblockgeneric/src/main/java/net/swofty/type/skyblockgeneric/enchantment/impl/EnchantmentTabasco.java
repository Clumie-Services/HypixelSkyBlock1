package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentTabasco implements Ench, EventBasedEnchant {

    public static final int[] DAMAGE_BONUS = new int[]{2, 2, 3};

    @Override
    public String getDescription(int level) {
        return "Grants §c+" + DAMAGE_BONUS[level - 1] + "§7 weapon damage if you don't have a Dragon pet equipped.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 50,
                2, 100,
                3, 200
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null) return ItemStatistics.empty();

        // Check if player has a Dragon pet equipped
        SkyBlockItem pet = causer.getPetData().getEnabledPet();
        if (pet != null) {
            String petName = pet.getAttributeHandler().getPotentialType().name().toLowerCase();
            if (petName.contains("dragon")) {
                return ItemStatistics.empty();
            }
        }

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, (double) DAMAGE_BONUS[level - 1]).build();
    }
}
