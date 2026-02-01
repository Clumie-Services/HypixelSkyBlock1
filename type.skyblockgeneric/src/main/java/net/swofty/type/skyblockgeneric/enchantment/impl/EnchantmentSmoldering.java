package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentSmoldering implements Ench, EventBasedEnchant {

    public static final int[] DAMAGE_BONUS = new int[]{3, 6, 9, 12, 15};

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt to Blazes by §a" + DAMAGE_BONUS[level - 1] + "%§7.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 50,
                2, 100,
                3, 200,
                4, 400,
                5, 800
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (receiver == null) return ItemStatistics.empty();

        // Check if target is a Blaze
        boolean isBlaze = receiver.getEntityType() == EntityType.BLAZE;

        // Also check SkyBlockMob type if applicable
        if (!isBlaze && receiver instanceof SkyBlockMob mob) {
            String mobName = mob.getDisplayName().toLowerCase();
            isBlaze = mobName.contains("blaze");
        }

        if (!isBlaze) return ItemStatistics.empty();

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, (double) DAMAGE_BONUS[level - 1]).build();
    }
}
