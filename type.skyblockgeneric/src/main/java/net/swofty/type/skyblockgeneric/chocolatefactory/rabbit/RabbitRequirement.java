package net.swofty.type.skyblockgeneric.chocolatefactory.rabbit;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.json.JSONObject;

/**
 * Represents a requirement that must be met to unlock/find a specific rabbit.
 */
@Getter
public class RabbitRequirement {

    public enum RequirementType {
        NONE,                    // No requirement, can be found normally
        BOSS_KILL,              // Must kill a specific boss
        ISLAND_VISIT,           // Must visit a specific island
        COLLECTION_LEVEL,       // Must reach a collection level
        SKILL_LEVEL,            // Must reach a skill level
        FACTORY_LEVEL,          // Must reach a specific CF level
        EVENT_ONLY,             // Only available during specific events
        HOPPITY_HUNT,           // Can only be found during Hoppity's Hunt
        SHOP_PURCHASE,          // Must be purchased from a shop
        SPECIAL                 // Special unlock condition (described in description)
    }

    private final RequirementType type;
    private final String value;
    private final String description;
    private final int amount;

    public RabbitRequirement(RequirementType type, String value, String description, int amount) {
        this.type = type;
        this.value = value;
        this.description = description;
        this.amount = amount;
    }

    public RabbitRequirement(RequirementType type) {
        this(type, null, null, 0);
    }

    /**
     * Creates a requirement from JSON
     */
    public static RabbitRequirement fromJson(JSONObject json) {
        if (json == null) return new RabbitRequirement(RequirementType.NONE);

        RequirementType type = RequirementType.valueOf(json.optString("type", "NONE"));
        String value = json.optString("value", null);
        String description = json.optString("description", null);
        int amount = json.optInt("amount", 0);

        return new RabbitRequirement(type, value, description, amount);
    }

    /**
     * Converts this requirement to JSON
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type.name());
        if (value != null) json.put("value", value);
        if (description != null) json.put("description", description);
        if (amount > 0) json.put("amount", amount);
        return json;
    }

    /**
     * Checks if a player meets this requirement
     */
    public boolean isMet(SkyBlockPlayer player) {
        return switch (type) {
            case NONE, HOPPITY_HUNT -> true;
            case BOSS_KILL -> checkBossKill(player);
            case ISLAND_VISIT -> checkIslandVisit(player);
            case COLLECTION_LEVEL -> checkCollectionLevel(player);
            case SKILL_LEVEL -> checkSkillLevel(player);
            case FACTORY_LEVEL -> checkFactoryLevel(player);
            case EVENT_ONLY -> checkEventActive();
            case SHOP_PURCHASE -> false; // Must be purchased, not found
            case SPECIAL -> checkSpecialRequirement(player);
        };
    }

    private boolean checkBossKill(SkyBlockPlayer player) {
        // Check bestiary for boss kill
        // This would integrate with the existing bestiary system
        return true; // Placeholder
    }

    private boolean checkIslandVisit(SkyBlockPlayer player) {
        // Check if player has visited the island
        var visitedIslands = player.getSkyblockDataHandler()
                .get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.VISITED_ISLANDS)
                .getValue();
        if (visitedIslands instanceof java.util.List<?> list) {
            return list.contains(value);
        }
        return false;
    }

    private boolean checkCollectionLevel(SkyBlockPlayer player) {
        // Check collection level
        // This would integrate with the existing collection system
        return true; // Placeholder
    }

    private boolean checkSkillLevel(SkyBlockPlayer player) {
        // Check skill level
        // This would integrate with the existing skill system
        return true; // Placeholder
    }

    private boolean checkFactoryLevel(SkyBlockPlayer player) {
        var factoryData = player.getChocolateFactoryData();
        return factoryData.getFactoryLevel() >= amount;
    }

    private boolean checkEventActive() {
        // Check if the required event is currently active
        // This would integrate with the event system
        return false; // Placeholder
    }

    private boolean checkSpecialRequirement(SkyBlockPlayer player) {
        // Special requirements are checked on a case-by-case basis
        return true; // Placeholder
    }

    /**
     * Gets the display description for this requirement
     */
    public String getDisplayDescription() {
        if (description != null) return description;

        return switch (type) {
            case NONE -> "Can be found anywhere!";
            case BOSS_KILL -> "Kill " + value + " to unlock";
            case ISLAND_VISIT -> "Visit " + value + " to unlock";
            case COLLECTION_LEVEL -> "Reach " + value + " collection level " + amount;
            case SKILL_LEVEL -> "Reach " + value + " level " + amount;
            case FACTORY_LEVEL -> "Reach Chocolate Factory " + amount;
            case EVENT_ONLY -> "Only available during events";
            case HOPPITY_HUNT -> "Found during Hoppity's Hunt";
            case SHOP_PURCHASE -> "Purchase from " + value;
            case SPECIAL -> "Special unlock requirement";
        };
    }
}
