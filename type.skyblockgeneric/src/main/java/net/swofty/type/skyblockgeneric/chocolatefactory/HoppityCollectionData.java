package net.swofty.type.skyblockgeneric.chocolatefactory;

import lombok.Getter;
import lombok.Setter;
import net.swofty.type.skyblockgeneric.chocolatefactory.rabbit.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.chocolatefactory.rabbit.ChocolateRabbitRarity;

import java.util.*;

/**
 * Holds data about a player's Hoppity rabbit collection.
 */
@Getter
@Setter
public class HoppityCollectionData {
    // Set of collected rabbit IDs
    private Set<String> collectedRabbitIds = new HashSet<>();

    // Count of duplicates found per rabbit (for bonus multipliers)
    private Map<String, Integer> rabbitDuplicates = new HashMap<>();

    // Total rabbits found (including duplicates)
    private int totalRabbitsFound = 0;

    // Statistics
    private int eggsFound = 0;
    private int strayRabbitsFound = 0;
    private int goldenRabbitsFound = 0;

    public HoppityCollectionData() {
    }

    /**
     * Adds a rabbit to the collection
     * @param rabbitId The ID of the rabbit
     * @return true if this was a new rabbit, false if duplicate
     */
    public boolean addRabbit(String rabbitId) {
        totalRabbitsFound++;
        String id = rabbitId.toUpperCase();

        if (collectedRabbitIds.contains(id)) {
            // Duplicate
            rabbitDuplicates.merge(id, 1, Integer::sum);
            return false;
        } else {
            // New rabbit
            collectedRabbitIds.add(id);
            rabbitDuplicates.put(id, 0);
            return true;
        }
    }

    /**
     * Checks if a rabbit has been collected
     */
    public boolean hasRabbit(String rabbitId) {
        return collectedRabbitIds.contains(rabbitId.toUpperCase());
    }

    /**
     * Gets the number of duplicates found for a rabbit
     */
    public int getDuplicateCount(String rabbitId) {
        return rabbitDuplicates.getOrDefault(rabbitId.toUpperCase(), 0);
    }

    /**
     * Gets the total number of unique rabbits collected
     */
    public int getUniqueRabbitCount() {
        return collectedRabbitIds.size();
    }

    /**
     * Gets the number of unique rabbits collected of a specific rarity
     */
    public int getUniqueRabbitCountByRarity(ChocolateRabbitRarity rarity) {
        int count = 0;
        for (String id : collectedRabbitIds) {
            ChocolateRabbit rabbit = ChocolateRabbit.getById(id);
            if (rabbit != null && rabbit.getRarity() == rarity) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the collection percentage
     */
    public double getCollectionPercentage() {
        int total = ChocolateRabbit.getTotalRabbitCount();
        if (total == 0) return 0;
        return (double) collectedRabbitIds.size() / total * 100;
    }

    /**
     * Gets the collection percentage for a specific rarity
     */
    public double getCollectionPercentageByRarity(ChocolateRabbitRarity rarity) {
        int total = ChocolateRabbit.getRabbitCountByRarity(rarity);
        if (total == 0) return 0;
        return (double) getUniqueRabbitCountByRarity(rarity) / total * 100;
    }

    /**
     * Gets all collected rabbits sorted by rarity (highest first)
     */
    public List<ChocolateRabbit> getCollectedRabbitsSorted() {
        List<ChocolateRabbit> rabbits = new ArrayList<>();
        for (String id : collectedRabbitIds) {
            ChocolateRabbit rabbit = ChocolateRabbit.getById(id);
            if (rabbit != null) {
                rabbits.add(rabbit);
            }
        }
        // Sort by rarity (highest first), then by name
        rabbits.sort((a, b) -> {
            int rarityCompare = Integer.compare(b.getRarity().getRarityValue(), a.getRarity().getRarityValue());
            if (rarityCompare != 0) return rarityCompare;
            return a.getName().compareTo(b.getName());
        });
        return rabbits;
    }

    /**
     * Gets uncollected rabbits sorted by rarity
     */
    public List<ChocolateRabbit> getUncollectedRabbitsSorted() {
        List<ChocolateRabbit> rabbits = new ArrayList<>();
        for (ChocolateRabbit rabbit : ChocolateRabbit.getAllRabbits()) {
            if (!collectedRabbitIds.contains(rabbit.getId().toUpperCase())) {
                rabbits.add(rabbit);
            }
        }
        // Sort by rarity (lowest first for easier collection targets), then by name
        rabbits.sort((a, b) -> {
            int rarityCompare = Integer.compare(a.getRarity().getRarityValue(), b.getRarity().getRarityValue());
            if (rarityCompare != 0) return rarityCompare;
            return a.getName().compareTo(b.getName());
        });
        return rabbits;
    }

    /**
     * Calculates the total CpS bonus from collected rabbits
     */
    public double getTotalRabbitCps() {
        double totalCps = 0;
        for (String id : collectedRabbitIds) {
            ChocolateRabbit rabbit = ChocolateRabbit.getById(id);
            if (rabbit != null) {
                totalCps += rabbit.getBaseCps();
            }
        }
        return totalCps;
    }

    /**
     * Calculates the total multiplier bonus from collected rabbits
     */
    public double getTotalRabbitMultiplier() {
        double totalMultiplier = 0;
        for (String id : collectedRabbitIds) {
            ChocolateRabbit rabbit = ChocolateRabbit.getById(id);
            if (rabbit != null) {
                totalMultiplier += rabbit.getMultiplierBonus();
                // Add extra multiplier from duplicates
                int dupes = getDuplicateCount(id);
                totalMultiplier += dupes * rabbit.getRarity().getExtraMultiplier();
            }
        }
        return totalMultiplier;
    }

    /**
     * Creates a copy of this data
     */
    public HoppityCollectionData copy() {
        HoppityCollectionData copy = new HoppityCollectionData();
        copy.collectedRabbitIds = new HashSet<>(this.collectedRabbitIds);
        copy.rabbitDuplicates = new HashMap<>(this.rabbitDuplicates);
        copy.totalRabbitsFound = this.totalRabbitsFound;
        copy.eggsFound = this.eggsFound;
        copy.strayRabbitsFound = this.strayRabbitsFound;
        copy.goldenRabbitsFound = this.goldenRabbitsFound;
        return copy;
    }
}
