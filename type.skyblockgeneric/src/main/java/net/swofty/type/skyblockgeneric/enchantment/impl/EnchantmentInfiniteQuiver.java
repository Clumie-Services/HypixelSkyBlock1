package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentInfiniteQuiver implements Ench, EnchFromTable {

    public static final int[] SAVE_CHANCE = new int[]{3, 6, 9, 12, 15, 18, 21, 24, 27, 30};

    @Override
    public String getDescription(int level) {
        return "Saves arrows §a" + SAVE_CHANCE[level - 1] + "%§7 of the time when you fire your bow. §8Disabled while sneaking.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.ofEntries(
                Map.entry(1, 5),
                Map.entry(2, 10),
                Map.entry(3, 15),
                Map.entry(4, 20),
                Map.entry(5, 25),
                Map.entry(6, 50),
                Map.entry(7, 100),
                Map.entry(8, 150),
                Map.entry(9, 200),
                Map.entry(10, 300)
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        return new TableLevels(new HashMap<>(Map.of(
                1, 10,
                2, 15,
                3, 20,
                4, 25,
                5, 30
        )));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }
}
