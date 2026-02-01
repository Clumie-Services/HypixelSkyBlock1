package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentDuplex implements Ench {

    public static final double[] SECOND_ARROW_DAMAGE = new double[]{4.0, 8.0, 12.0, 16.0, 20.0};
    public static final double[] FIRE_DAMAGE_MULTIPLIERS = new double[]{1.1, 1.2, 1.3, 1.4, 1.5};

    @Override
    public String getDescription(int level) {
        return "Shoot a second arrow dealing §a" + (int) SECOND_ARROW_DAMAGE[level - 1] + "%§7 of the first arrow's damage. " +
                "Targets hit take §a" + FIRE_DAMAGE_MULTIPLIERS[level - 1] + "x§7 fire damage for §a60s§7.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 20,
                2, 40,
                3, 60,
                4, 80,
                5, 100
        ));

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.empty();
    }
}
