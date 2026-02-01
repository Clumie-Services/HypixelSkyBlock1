package net.swofty.type.generic.entity.npc.json;

import java.util.List;

/**
 * JSON configuration model for NPCs.
 * Supports three NPC types: human, villager, and animal.
 */
public record NPCJsonConfig(List<NPCData> npcs) {

    /**
     * Data for a single NPC from JSON.
     *
     * @param id         Unique identifier for the NPC
     * @param name       Display name shown in chat
     * @param type       NPC type: "human", "villager", or "animal"
     * @param position   Position and rotation data
     * @param skin       Skin data for human NPCs (texture and signature)
     * @param profession Villager profession for villager NPCs
     * @param entityType Entity type for animal NPCs (e.g., "SNOW_GOLEM", "COW")
     * @param holograms  Text lines displayed above the NPC
     * @param looking    Whether the NPC should look at nearby players
     * @param hologramYOffset Y offset for hologram positioning (for animal NPCs)
     */
    public record NPCData(
            String id,
            String name,
            String type,
            PositionData position,
            SkinData skin,
            String profession,
            String entityType,
            List<String> holograms,
            Boolean looking,
            Float hologramYOffset
    ) {
        /**
         * Returns whether the NPC should look at players.
         * Defaults to false if not specified.
         */
        public boolean shouldLook() {
            return looking != null && looking;
        }

        /**
         * Returns the hologram Y offset.
         * Defaults to 0 if not specified.
         */
        public float getHologramYOffset() {
            return hologramYOffset != null ? hologramYOffset : 0.0f;
        }
    }

    /**
     * Position and rotation data for an NPC.
     */
    public record PositionData(
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
    }

    /**
     * Skin data for human NPCs.
     */
    public record SkinData(
            String texture,
            String signature
    ) {
    }
}
