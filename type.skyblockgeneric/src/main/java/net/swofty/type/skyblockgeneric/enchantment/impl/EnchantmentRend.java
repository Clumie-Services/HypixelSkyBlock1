package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentRend implements Ench {

    public static final int[] DAMAGE_PER_ARROW = new int[]{5, 10, 15, 20, 25};
    public static final int MAX_ARROWS = 5;
    public static final int COOLDOWN_SECONDS = 2;

    @Override
    public String getDescription(int level) {
        return "Use §eLeft Click§7 ability to rip your arrows out of nearby enemies. " +
                "Each arrow deals §a" + DAMAGE_PER_ARROW[level - 1] + "%§7 of your last critical shot on the target, " +
                "up to §a" + MAX_ARROWS + "§7 arrows. §8" + COOLDOWN_SECONDS + "s Cooldown.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 400,
                2, 800,
                3, 1600,
                4, 3200,
                5, 6400
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }
}
