package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentDragonTracer implements Ench {

    public static final int[] HOMING_RANGE = new int[]{2, 4, 6, 8, 10};

    @Override
    public String getDescription(int level) {
        return "Arrows home towards dragons if they are within §a" + HOMING_RANGE[level - 1] + "§7 blocks.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 100,
                2, 200,
                3, 400,
                4, 800,
                5, 1600
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }
}
