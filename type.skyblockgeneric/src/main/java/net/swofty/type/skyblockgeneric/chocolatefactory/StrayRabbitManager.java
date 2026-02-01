package net.swofty.type.skyblockgeneric.chocolatefactory;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.swofty.type.skyblockgeneric.chocolatefactory.rabbit.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.chocolatefactory.rabbit.ChocolateRabbitRarity;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHoppityCollection;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages stray rabbit spawning in the Chocolate Factory GUI.
 * Stray rabbits appear in empty slots and grant chocolate when clicked.
 * Golden rabbits are rare variants that give extra bonuses.
 */
public class StrayRabbitManager {
    private static final Random RANDOM = new Random();

    // Stray rabbit textures
    private static final String STRAY_RABBIT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBjZWZhNjdlNzU2NDJkMjY5MzM4MzY5ZTJmNjJlNzY3ZDQ5NDVjZTcyMjNjNmQyZTZlMGYxYTFhNjdkNjg0YyJ9fX0=";
    private static final String GOLDEN_RABBIT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFiYjE2ZDQ4ZTg2YzQxNjY0MTQyNmNlOTQxNDE4OGVmYTI0YTQ4N2VlMmJhNTdkYzM2YTI0ZWE4ZjVhYjNiMyJ9fX0=";

    // Spawn chances
    private static final double BASE_STRAY_CHANCE = 0.05; // 5% base chance per empty slot per refresh
    private static final double GOLDEN_RABBIT_CHANCE = 0.01; // 1% chance for golden variant

    // Slots where stray rabbits can appear (empty slots in factory GUI)
    private static final int[] STRAY_SLOTS = {
            0, 1, 2, 3, 5, 6, 7, 8, 9,
            10, 11, 12, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            36, 37, 40, 43, 44
    };

    /**
     * Represents a stray rabbit that appeared in the GUI.
     */
    @Getter
    public static class StrayRabbit {
        private final int slot;
        private final boolean golden;
        private final ChocolateRabbit rabbit;
        private final long chocolateReward;
        private final long spawnTime;

        public StrayRabbit(int slot, boolean golden, ChocolateRabbit rabbit, long chocolateReward) {
            this.slot = slot;
            this.golden = golden;
            this.rabbit = rabbit;
            this.chocolateReward = chocolateReward;
            this.spawnTime = System.currentTimeMillis();
        }

        /**
         * Checks if this stray rabbit has expired (15 seconds lifetime)
         */
        public boolean isExpired() {
            return System.currentTimeMillis() - spawnTime > 15000;
        }
    }

    /**
     * Attempts to spawn stray rabbits for a player's factory GUI.
     * Returns a list of GUI items for the spawned rabbits.
     *
     * @param player The player
     * @param usedSlots Slots already in use by the GUI
     * @return List of spawned stray rabbits
     */
    public static List<StrayRabbit> attemptSpawnStrayRabbits(SkyBlockPlayer player, int[] usedSlots) {
        List<StrayRabbit> strays = new ArrayList<>();
        ChocolateFactoryData data = player.getChocolateFactoryData();
        HoppityCollectionData collection = player.getHoppityCollectionData();

        // Calculate spawn chance modifier from milestones
        double spawnChanceModifier = getSpawnChanceModifier(data);

        // Get available slots
        List<Integer> availableSlots = new ArrayList<>();
        for (int slot : STRAY_SLOTS) {
            boolean inUse = false;
            for (int used : usedSlots) {
                if (used == slot) {
                    inUse = true;
                    break;
                }
            }
            if (!inUse) {
                availableSlots.add(slot);
            }
        }

        // Try to spawn in each available slot
        for (int slot : availableSlots) {
            if (RANDOM.nextDouble() < BASE_STRAY_CHANCE * spawnChanceModifier) {
                // Spawn a stray rabbit
                boolean golden = RANDOM.nextDouble() < GOLDEN_RABBIT_CHANCE;
                ChocolateRabbit rabbit = selectRandomRabbit(player, golden);
                long reward = calculateReward(rabbit, golden, data);

                strays.add(new StrayRabbit(slot, golden, rabbit, reward));

                // Limit to 2-3 strays at a time
                if (strays.size() >= 3) break;
            }
        }

        return strays;
    }

    /**
     * Creates a GUI item for a stray rabbit.
     */
    public static GUIClickableItem createStrayRabbitItem(StrayRabbit stray) {
        return new GUIClickableItem(stray.getSlot()) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ChocolateFactoryData data = player.getChocolateFactoryData();
                HoppityCollectionData collection = player.getHoppityCollectionData();

                // Check if still valid
                if (stray.isExpired()) {
                    player.sendMessage("§cThe rabbit escaped!");
                    return;
                }

                // Add chocolate reward
                data.addChocolate(stray.getChocolateReward());

                // Try to add rabbit to collection
                boolean newRabbit = collection.addRabbit(stray.getRabbit().getId());
                collection.setStrayRabbitsFound(collection.getStrayRabbitsFound() + 1);
                if (stray.isGolden()) {
                    collection.setGoldenRabbitsFound(collection.getGoldenRabbitsFound() + 1);
                }

                // Save data
                player.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                        .setValue(data);
                player.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.HOPPITY_COLLECTION, DatapointHoppityCollection.class)
                        .setValue(collection);

                // Send messages and play sounds
                if (stray.isGolden()) {
                    player.sendMessage("§6§lGOLDEN RABBIT! §eYou caught " + stray.getRabbit().getFormattedName() +
                            " §eand received §6" + StringUtility.commaify(stray.getChocolateReward()) + " Chocolate§e!");
                    player.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1f, 1.5f), Sound.Emitter.self());
                } else {
                    player.sendMessage("§aYou caught " + stray.getRabbit().getFormattedName() +
                            " §aand received §6" + StringUtility.commaify(stray.getChocolateReward()) + " Chocolate§a!");
                    player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1.5f), Sound.Emitter.self());
                }

                if (newRabbit) {
                    player.sendMessage("§d§lNEW RABBIT! §dAdded to your collection!");
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                String texture = stray.isGolden() ? GOLDEN_RABBIT_TEXTURE : STRAY_RABBIT_TEXTURE;
                String name = stray.isGolden() ? "§6§lGOLDEN STRAY RABBIT!" : "§aStray Rabbit";

                List<String> lore = new ArrayList<>();
                lore.add(stray.getRabbit().getRarity().getFormattedName() + " " + stray.getRabbit().getName());
                lore.add("");
                lore.add("§7Reward: §6" + StringUtility.commaify(stray.getChocolateReward()) + " Chocolate");
                lore.add("");
                if (stray.isGolden()) {
                    lore.add("§6✦ Golden rabbits give bonus rewards!");
                    lore.add("");
                }
                lore.add("§eClick to catch!");
                lore.add("§c⚠ Escapes in " + Math.max(0, 15 - (System.currentTimeMillis() - stray.getSpawnTime()) / 1000) + "s!");

                return ItemStackCreator.getStackHead(name, texture, 1, lore.toArray(new String[0]));
            }
        };
    }

    /**
     * Selects a random rabbit for a stray spawn.
     */
    private static ChocolateRabbit selectRandomRabbit(SkyBlockPlayer player, boolean golden) {
        ChocolateFactoryData data = player.getChocolateFactoryData();

        // Shrine level affects rarity chances
        int shrineLevel = data.getRabbitShrineLevel();
        double rarityBonus = shrineLevel * 0.05; // 5% per level

        // Golden rabbits get bonus rarity
        if (golden) {
            rarityBonus += 0.5;
        }

        // Weighted random selection
        if (RANDOM.nextDouble() < rarityBonus) {
            // Higher rarity
            ChocolateRabbitRarity[] higherRarities = {
                    ChocolateRabbitRarity.RARE,
                    ChocolateRabbitRarity.EPIC,
                    ChocolateRabbitRarity.LEGENDARY
            };
            ChocolateRabbitRarity rarity = higherRarities[RANDOM.nextInt(higherRarities.length)];
            ChocolateRabbit rabbit = ChocolateRabbit.getRandomRabbitOfRarity(rarity, RANDOM);
            if (rabbit != null) return rabbit;
        }

        // Default random
        ChocolateRabbit rabbit = ChocolateRabbit.getRandomRabbit(RANDOM);
        return rabbit != null ? rabbit : ChocolateRabbit.getById("AARON"); // Fallback
    }

    /**
     * Calculates the chocolate reward for catching a stray rabbit.
     */
    private static long calculateReward(ChocolateRabbit rabbit, boolean golden, ChocolateFactoryData data) {
        long baseReward = rabbit.getRarity().getChocolateReward();

        // Factory level multiplier
        double factoryMultiplier = ChocolateCalculator.getFactoryProductionMultiplier(data.getFactoryLevel());
        baseReward = (long) (baseReward * factoryMultiplier);

        // Golden bonus (5x)
        if (golden) {
            baseReward *= 5;
        }

        return baseReward;
    }

    /**
     * Gets the spawn chance modifier from milestones.
     */
    private static double getSpawnChanceModifier(ChocolateFactoryData data) {
        double modifier = 1.0;

        // Add milestone bonuses (from factory milestones)
        long allTime = data.getAllTimeChocolate();
        if (allTime >= 2_500_000) modifier += 0.05;  // Milestone 4
        if (allTime >= 50_000_000) modifier += 0.05; // Milestone 8
        if (allTime >= 1_500_000_000) modifier += 0.05; // Milestone 15
        if (allTime >= 6_000_000_000L) modifier += 0.05; // Milestone 18
        if (allTime >= 15_000_000_000L) modifier += 0.05; // Milestone 21
        if (allTime >= 40_000_000_000L) modifier += 0.05; // Milestone 23

        return modifier;
    }
}
