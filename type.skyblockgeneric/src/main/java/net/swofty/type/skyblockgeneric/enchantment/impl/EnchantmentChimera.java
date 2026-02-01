package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentChimera implements Ench, EventBasedEnchant {

    public static final int[] COPY_PERCENTAGES = new int[]{20, 40, 60, 80, 100};

    @Override
    public String getDescription(int level) {
        return "Copies §a" + COPY_PERCENTAGES[level - 1] + "%§7 of your active pet's stats.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 500,
                2, 1000,
                3, 2000,
                4, 4000,
                5, 8000
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null) return ItemStatistics.empty();

        SkyBlockItem pet = causer.getPetData().getEnabledPet();
        if (pet == null || !pet.hasComponent(PetComponent.class)) return ItemStatistics.empty();

        ItemStatistics petStats = causer.getStatistics().petStatistics();
        double multiplier = COPY_PERCENTAGES[level - 1] / 100.0;

        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic stat : ItemStatistic.values()) {
            double value = petStats.getOverall(stat) * multiplier;
            if (value != 0) {
                builder.withBase(stat, value);
            }
        }

        return builder.build();
    }
}
