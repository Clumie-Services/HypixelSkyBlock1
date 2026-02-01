package net.swofty.type.generic.entity.npc.json;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.configuration.NPCConfiguration;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

/**
 * An NPC implementation that is configured via JSON data.
 * This NPC is purely for display purposes - it does not have any click behavior.
 * For NPCs that need custom behavior, create a traditional Java NPC class.
 */
public class JsonConfiguredNPC extends HypixelNPC {
    private final String id;

    /**
     * Creates a JSON-configured NPC from the provided data.
     *
     * @param data The NPC data from JSON
     */
    public JsonConfiguredNPC(NPCJsonConfig.NPCData data) {
        super(createConfiguration(data));
        this.id = data.id();
    }

    /**
     * Gets the unique identifier for this NPC.
     *
     * @return The NPC id from JSON
     */
    public String getId() {
        return id;
    }

    /**
     * JSON-configured NPCs do not have click behavior.
     * Override this in a subclass or use traditional Java NPCs for interactive NPCs.
     */
    @Override
    public void onClick(NPCInteractEvent event) {
        // No default behavior for JSON-configured NPCs
    }

    /**
     * Creates the appropriate NPCConfiguration based on the NPC type.
     */
    private static NPCConfiguration createConfiguration(NPCJsonConfig.NPCData data) {
        String type = data.type().toLowerCase();

        return switch (type) {
            case "human" -> createHumanConfiguration(data);
            case "villager" -> createVillagerConfiguration(data);
            case "animal" -> createAnimalConfiguration(data);
            default -> throw new IllegalArgumentException("Unknown NPC type: " + type + " for NPC: " + data.id());
        };
    }

    private static HumanConfiguration createHumanConfiguration(NPCJsonConfig.NPCData data) {
        if (data.skin() == null) {
            throw new IllegalArgumentException("Human NPC '" + data.id() + "' requires skin data (texture and signature)");
        }

        final Pos pos = new Pos(
                data.position().x(),
                data.position().y(),
                data.position().z(),
                data.position().yaw(),
                data.position().pitch()
        );
        final String[] hologramArray = data.holograms() != null
                ? data.holograms().toArray(String[]::new)
                : new String[]{data.name()};
        final boolean looking = data.shouldLook();
        final String texture = data.skin().texture();
        final String signature = data.skin().signature();
        final String chatName = data.name();

        return new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return hologramArray;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return pos;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return looking;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return texture;
            }

            @Override
            public String signature(HypixelPlayer player) {
                return signature;
            }

            @Override
            public String chatName() {
                return chatName;
            }
        };
    }

    private static VillagerConfiguration createVillagerConfiguration(NPCJsonConfig.NPCData data) {
        if (data.profession() == null) {
            throw new IllegalArgumentException("Villager NPC '" + data.id() + "' requires a profession");
        }

        final Pos pos = new Pos(
                data.position().x(),
                data.position().y(),
                data.position().z(),
                data.position().yaw(),
                data.position().pitch()
        );
        final String[] hologramArray = data.holograms() != null
                ? data.holograms().toArray(String[]::new)
                : new String[]{data.name()};
        final boolean looking = data.shouldLook();
        final VillagerProfession profession = parseVillagerProfession(data.profession());
        final String chatName = data.name();

        return new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return hologramArray;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return pos;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return looking;
            }

            @Override
            public VillagerProfession profession() {
                return profession;
            }

            @Override
            public String chatName() {
                return chatName;
            }
        };
    }

    private static AnimalConfiguration createAnimalConfiguration(NPCJsonConfig.NPCData data) {
        if (data.entityType() == null) {
            throw new IllegalArgumentException("Animal NPC '" + data.id() + "' requires an entityType");
        }

        final Pos pos = new Pos(
                data.position().x(),
                data.position().y(),
                data.position().z(),
                data.position().yaw(),
                data.position().pitch()
        );
        final String[] hologramArray = data.holograms() != null
                ? data.holograms().toArray(String[]::new)
                : new String[]{data.name()};
        final boolean looking = data.shouldLook();
        final EntityType entityType = parseEntityType(data.entityType());
        final float yOffset = data.getHologramYOffset();
        final String chatName = data.name();

        return new AnimalConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return hologramArray;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return pos;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return looking;
            }

            @Override
            public EntityType entityType() {
                return entityType;
            }

            @Override
            public float hologramYOffset() {
                return yOffset;
            }

            @Override
            public String chatName() {
                return chatName;
            }
        };
    }

    private static VillagerProfession parseVillagerProfession(String profession) {
        return switch (profession.toUpperCase()) {
            case "NONE" -> VillagerProfession.NONE;
            case "ARMORER" -> VillagerProfession.ARMORER;
            case "BUTCHER" -> VillagerProfession.BUTCHER;
            case "CARTOGRAPHER" -> VillagerProfession.CARTOGRAPHER;
            case "CLERIC" -> VillagerProfession.CLERIC;
            case "FARMER" -> VillagerProfession.FARMER;
            case "FISHERMAN" -> VillagerProfession.FISHERMAN;
            case "FLETCHER" -> VillagerProfession.FLETCHER;
            case "LEATHERWORKER" -> VillagerProfession.LEATHERWORKER;
            case "LIBRARIAN" -> VillagerProfession.LIBRARIAN;
            case "MASON", "STONEMASON" -> VillagerProfession.MASON;
            case "NITWIT" -> VillagerProfession.NITWIT;
            case "SHEPHERD" -> VillagerProfession.SHEPHERD;
            case "TOOLSMITH" -> VillagerProfession.TOOLSMITH;
            case "WEAPONSMITH" -> VillagerProfession.WEAPONSMITH;
            default -> throw new IllegalArgumentException("Unknown villager profession: " + profession);
        };
    }

    private static EntityType parseEntityType(String entityTypeName) {
        // Use reflection to find the EntityType constant by name
        String normalizedName = entityTypeName.toUpperCase().replace("-", "_").replace(" ", "_");
        try {
            java.lang.reflect.Field field = EntityType.class.getField(normalizedName);
            return (EntityType) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unknown entity type: " + entityTypeName +
                    ". Use Minecraft entity type names like COW, WITCH, SNOW_GOLEM, etc.", e);
        }
    }
}
