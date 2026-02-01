package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.KillEventEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentSoulEater implements Ench, KillEventEnchant, EventBasedEnchant {

    public static final int[] DAMAGE_MULTIPLIERS = new int[]{2, 4, 6, 8, 10};

    private static final Map<UUID, SoulEaterData> playerData = new ConcurrentHashMap<>();

    @Override
    public String getDescription(int level) {
        return "Your weapon gains §c" + DAMAGE_MULTIPLIERS[level - 1] +
                "x§7 the Damage of the latest monster killed and applies it on your next hit.";
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
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.BOW);
    }

    @Override
    public void onMobKilled(SkyBlockPlayer player, SkyBlockMob killedMob, int level) {
        UUID playerId = player.getUuid();

        // Get the mob's base damage (using its max health as approximation for damage)
        double mobDamage = killedMob.getBaseStatistics().getOverall(ItemStatistic.DAMAGE);
        if (mobDamage <= 0) {
            // Fallback: use a portion of max health as damage estimate
            mobDamage = killedMob.getAttributeValue(Attribute.MAX_HEALTH) * 0.1;
        }

        double storedDamage = mobDamage * DAMAGE_MULTIPLIERS[level - 1];

        playerData.put(playerId, new SoulEaterData(storedDamage, true));
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null) return ItemStatistics.empty();

        UUID playerId = causer.getUuid();
        SoulEaterData data = playerData.get(playerId);

        if (data == null || !data.hasStoredSoul) return ItemStatistics.empty();

        // Consume the stored soul on use
        double damage = data.storedDamage;
        data.hasStoredSoul = false;
        data.storedDamage = 0;

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, damage).build();
    }

    private static class SoulEaterData {
        double storedDamage;
        boolean hasStoredSoul;

        SoulEaterData(double storedDamage, boolean hasStoredSoul) {
            this.storedDamage = storedDamage;
            this.hasStoredSoul = hasStoredSoul;
        }
    }
}
