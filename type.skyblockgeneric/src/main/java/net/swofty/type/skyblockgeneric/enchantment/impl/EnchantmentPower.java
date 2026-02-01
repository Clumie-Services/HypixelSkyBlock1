package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentPower implements Ench, EnchFromTable {

    public static final int[] DAMAGE_MULTIPLIERS = new int[]{8, 16, 25, 35, 45, 55, 65};

    @Override
    public String getDescription(int level) {
        return "Increases bow damage by §a" + DAMAGE_MULTIPLIERS[level - 1] + "%§7.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 9,
                2, 14,
                3, 18,
                4, 23,
                5, 27,
                6, 55,
                7, 179
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        int increase = DAMAGE_MULTIPLIERS[level - 1];
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, (double) increase).build();
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        return new TableLevels(new HashMap<>(Map.of(
                1, 10,
                2, 15,
                3, 20,
                4, 25,
                5, 30,
                6, 50,
                7, 100
        )));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }
}
