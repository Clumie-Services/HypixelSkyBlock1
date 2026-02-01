package net.swofty.type.generic.entity.npc.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.configuration.NPCConfiguration;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads NPCs from JSON configuration files.
 * JSON files define NPC appearance and position, while behavior remains in code.
 */
public class NPCJsonLoader {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    // Maps NPC ID to its source file path
    private static final Map<String, String> npcSourceFiles = new HashMap<>();
    // Maps file path to its loaded config (for saving back)
    private static final Map<String, NPCJsonConfig> loadedConfigs = new HashMap<>();

    /**
     * Loads NPCs from a JSON file and registers them.
     * The file path is relative to the working directory.
     *
     * @param filePath Path to the JSON file (e.g., "configuration/hub/npcs.json")
     * @return List of loaded NPCs
     */
    public static List<HypixelNPC> loadFromFile(String filePath) {
        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            Logger.debug("NPC JSON file not found: {} - skipping JSON NPC loading", filePath);
            return Collections.emptyList();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return loadFromReader(reader, filePath, true);
        } catch (IOException e) {
            Logger.error("Failed to read NPC JSON file: {}", filePath, e);
            return Collections.emptyList();
        }
    }

    /**
     * Loads NPCs from a JSON file in the classpath resources.
     *
     * @param resourcePath Path to the resource (e.g., "/npcs.json")
     * @return List of loaded NPCs
     */
    public static List<HypixelNPC> loadFromResource(String resourcePath) {
        try (InputStream is = NPCJsonLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                Logger.debug("NPC JSON resource not found: {} - skipping JSON NPC loading", resourcePath);
                return Collections.emptyList();
            }

            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return loadFromReader(reader, resourcePath, false);
            }
        } catch (IOException e) {
            Logger.error("Failed to read NPC JSON resource: {}", resourcePath, e);
            return Collections.emptyList();
        }
    }

    /**
     * Loads NPCs from a JSON string.
     *
     * @param json The JSON string containing NPC configuration
     * @return List of loaded NPCs
     */
    public static List<HypixelNPC> loadFromString(String json) {
        try {
            NPCJsonConfig config = GSON.fromJson(json, NPCJsonConfig.class);
            return createNPCs(config, "string input", false);
        } catch (Exception e) {
            Logger.error("Failed to parse NPC JSON string", e);
            return Collections.emptyList();
        }
    }

    /**
     * Loads and registers NPCs from a JSON file.
     * Convenience method that both loads and registers NPCs.
     *
     * @param filePath Path to the JSON file
     * @return Number of NPCs loaded and registered
     */
    public static int loadAndRegister(String filePath) {
        List<HypixelNPC> npcs = loadFromFile(filePath);
        npcs.forEach(HypixelNPC::register);
        if (!npcs.isEmpty()) {
            Logger.info("Loaded and registered {} JSON-configured NPCs from {}", npcs.size(), filePath);
        }
        return npcs.size();
    }

    private static List<HypixelNPC> loadFromReader(Reader reader, String sourceName, boolean isFile) {
        try {
            NPCJsonConfig config = GSON.fromJson(reader, NPCJsonConfig.class);
            if (isFile) {
                loadedConfigs.put(sourceName, config);
            }
            return createNPCs(config, sourceName, isFile);
        } catch (Exception e) {
            Logger.error("Failed to parse NPC JSON from: {}", sourceName, e);
            return Collections.emptyList();
        }
    }

    private static List<HypixelNPC> createNPCs(NPCJsonConfig config, String sourceName, boolean trackSource) {
        if (config == null || config.npcs() == null || config.npcs().isEmpty()) {
            Logger.debug("No NPCs found in JSON config: {}", sourceName);
            return Collections.emptyList();
        }

        List<HypixelNPC> npcs = new ArrayList<>();

        for (NPCJsonConfig.NPCData data : config.npcs()) {
            try {
                validateNPCData(data);
                JsonConfiguredNPC npc = new JsonConfiguredNPC(data);
                npcs.add(npc);
                if (trackSource) {
                    npcSourceFiles.put(data.id(), sourceName);
                }
                Logger.debug("Created JSON NPC: {} (type: {})", data.id(), data.type());
            } catch (Exception e) {
                Logger.error("Failed to create NPC '{}' from {}: {}", data.id(), sourceName, e.getMessage());
            }
        }

        return npcs;
    }

    private static void validateNPCData(NPCJsonConfig.NPCData data) {
        if (data.id() == null || data.id().isBlank()) {
            throw new IllegalArgumentException("NPC id is required");
        }
        if (data.name() == null || data.name().isBlank()) {
            throw new IllegalArgumentException("NPC name is required");
        }
        if (data.type() == null || data.type().isBlank()) {
            throw new IllegalArgumentException("NPC type is required");
        }
        if (data.position() == null) {
            throw new IllegalArgumentException("NPC position is required");
        }
    }

    /**
     * Updates the position of a JSON-configured NPC and saves it back to the file.
     *
     * @param npcId    The NPC's unique identifier
     * @param position The new position
     * @return true if saved successfully, false otherwise
     */
    public static boolean updateNPCPosition(String npcId, Pos position) {
        String filePath = npcSourceFiles.get(npcId);
        if (filePath == null) {
            Logger.error("Cannot update NPC '{}' - not loaded from a JSON file", npcId);
            return false;
        }

        NPCJsonConfig config = loadedConfigs.get(filePath);
        if (config == null || config.npcs() == null) {
            Logger.error("Cannot update NPC '{}' - config not found for file {}", npcId, filePath);
            return false;
        }

        // Find and update the NPC
        List<NPCJsonConfig.NPCData> updatedNpcs = new ArrayList<>();
        boolean found = false;
        for (NPCJsonConfig.NPCData npc : config.npcs()) {
            if (npc.id().equals(npcId)) {
                // Create new NPCData with updated position
                NPCJsonConfig.PositionData newPosition = new NPCJsonConfig.PositionData(
                        position.x(),
                        position.y(),
                        position.z(),
                        position.yaw(),
                        position.pitch()
                );
                NPCJsonConfig.NPCData updatedNpc = new NPCJsonConfig.NPCData(
                        npc.id(),
                        npc.name(),
                        npc.type(),
                        newPosition,
                        npc.skin(),
                        npc.profession(),
                        npc.entityType(),
                        npc.holograms(),
                        npc.looking(),
                        npc.hologramYOffset()
                );
                updatedNpcs.add(updatedNpc);
                found = true;
            } else {
                updatedNpcs.add(npc);
            }
        }

        if (!found) {
            Logger.error("Cannot update NPC '{}' - not found in config", npcId);
            return false;
        }

        // Save back to file
        NPCJsonConfig updatedConfig = new NPCJsonConfig(updatedNpcs);
        loadedConfigs.put(filePath, updatedConfig);

        return saveToFile(filePath, updatedConfig);
    }

    /**
     * Saves the NPC configuration to a file.
     *
     * @param filePath The file path to save to
     * @param config   The configuration to save
     * @return true if saved successfully
     */
    public static boolean saveToFile(String filePath, NPCJsonConfig config) {
        Path path = Path.of(filePath);
        try {
            // Ensure parent directories exist
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
            Logger.info("Saved NPC config to {}", filePath);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to save NPC config to {}", filePath, e);
            return false;
        }
    }

    /**
     * Gets the source file path for an NPC.
     *
     * @param npcId The NPC's unique identifier
     * @return The file path, or null if not found
     */
    public static String getSourceFile(String npcId) {
        return npcSourceFiles.get(npcId);
    }

    /**
     * Checks if an NPC was loaded from a JSON file.
     *
     * @param npcId The NPC's unique identifier
     * @return true if the NPC was loaded from JSON
     */
    public static boolean isJsonConfigured(String npcId) {
        return npcSourceFiles.containsKey(npcId);
    }

    /**
     * Exports a Java-coded NPC to JSON format.
     * This converts the NPC's configuration into NPCJsonConfig.NPCData.
     *
     * @param npc    The NPC to export
     * @param id     The unique ID to assign to the exported NPC
     * @param player The player to use for resolving dynamic values (can be null for static NPCs)
     * @return The NPC data ready for JSON serialization
     */
    public static NPCJsonConfig.NPCData exportNPCToData(HypixelNPC npc, String id, @Nullable HypixelPlayer player) {
        NPCConfiguration config = npc.getParameters();
        Pos pos = config.position(player);
        String[] holograms = config.holograms(player);
        boolean looking = config.looking(player);
        String name = npc.getName();

        NPCJsonConfig.PositionData positionData = new NPCJsonConfig.PositionData(
                pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch()
        );

        String type;
        NPCJsonConfig.SkinData skinData = null;
        String profession = null;
        String entityType = null;
        Float hologramYOffset = null;

        if (config instanceof HumanConfiguration humanConfig) {
            type = "human";
            skinData = new NPCJsonConfig.SkinData(
                    humanConfig.texture(player),
                    humanConfig.signature(player)
            );
        } else if (config instanceof VillagerConfiguration villagerConfig) {
            type = "villager";
            profession = villagerConfig.profession().name();
        } else if (config instanceof AnimalConfiguration animalConfig) {
            type = "animal";
            entityType = animalConfig.entityType().name();
            hologramYOffset = animalConfig.hologramYOffset();
        } else {
            throw new IllegalArgumentException("Unknown NPC configuration type: " + config.getClass().getName());
        }

        return new NPCJsonConfig.NPCData(
                id,
                name,
                type,
                positionData,
                skinData,
                profession,
                entityType,
                Arrays.asList(holograms),
                looking,
                hologramYOffset
        );
    }

    /**
     * Adds an NPC to a JSON file. If the file doesn't exist, it will be created.
     * If an NPC with the same ID already exists, it will be replaced.
     *
     * @param filePath The path to the JSON file
     * @param npcData  The NPC data to add
     * @return true if saved successfully
     */
    public static boolean addNPCToFile(String filePath, NPCJsonConfig.NPCData npcData) {
        Path path = Path.of(filePath);
        NPCJsonConfig config;

        // Load existing config or create new one
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, NPCJsonConfig.class);
                if (config == null || config.npcs() == null) {
                    config = new NPCJsonConfig(new ArrayList<>());
                }
            } catch (IOException e) {
                Logger.error("Failed to read existing NPC JSON file: {}", filePath, e);
                return false;
            }
        } else {
            config = new NPCJsonConfig(new ArrayList<>());
        }

        // Remove existing NPC with same ID if present, then add the new one
        List<NPCJsonConfig.NPCData> npcs = new ArrayList<>(config.npcs());
        npcs.removeIf(npc -> npc.id().equals(npcData.id()));
        npcs.add(npcData);

        NPCJsonConfig updatedConfig = new NPCJsonConfig(npcs);
        loadedConfigs.put(filePath, updatedConfig);
        npcSourceFiles.put(npcData.id(), filePath);

        return saveToFile(filePath, updatedConfig);
    }

    /**
     * Exports an existing Java NPC directly to a JSON file.
     *
     * @param npc      The NPC to export
     * @param id       The unique ID for the NPC
     * @param filePath The file to save to
     * @param player   The player for resolving dynamic values (can be null)
     * @return true if exported successfully
     */
    public static boolean exportNPCToFile(HypixelNPC npc, String id, String filePath, @Nullable HypixelPlayer player) {
        NPCJsonConfig.NPCData data = exportNPCToData(npc, id, player);
        return addNPCToFile(filePath, data);
    }

    /**
     * Generates a unique ID for an NPC based on its class name.
     *
     * @param npc The NPC to generate an ID for
     * @return A unique ID string
     */
    public static String generateNPCId(HypixelNPC npc) {
        String className = npc.getClass().getSimpleName();
        // Convert CamelCase to snake_case and add prefix
        String snakeCase = className
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
        return "npc_" + snakeCase;
    }

    /**
     * Updates the skin of a JSON-configured NPC and saves it back to the file.
     *
     * @param npcId     The NPC's unique identifier
     * @param texture   The new skin texture (base64)
     * @param signature The new skin signature
     * @return true if saved successfully, false otherwise
     */
    public static boolean updateNPCSkin(String npcId, String texture, String signature) {
        String filePath = npcSourceFiles.get(npcId);
        if (filePath == null) {
            Logger.error("Cannot update NPC '{}' skin - not loaded from a JSON file", npcId);
            return false;
        }

        NPCJsonConfig config = loadedConfigs.get(filePath);
        if (config == null || config.npcs() == null) {
            Logger.error("Cannot update NPC '{}' skin - config not found for file {}", npcId, filePath);
            return false;
        }

        // Find and update the NPC
        List<NPCJsonConfig.NPCData> updatedNpcs = new ArrayList<>();
        boolean found = false;
        for (NPCJsonConfig.NPCData npc : config.npcs()) {
            if (npc.id().equals(npcId)) {
                if (!"human".equalsIgnoreCase(npc.type())) {
                    Logger.error("Cannot update skin for NPC '{}' - not a human NPC", npcId);
                    return false;
                }
                // Create new NPCData with updated skin
                NPCJsonConfig.SkinData newSkin = new NPCJsonConfig.SkinData(texture, signature);
                NPCJsonConfig.NPCData updatedNpc = new NPCJsonConfig.NPCData(
                        npc.id(),
                        npc.name(),
                        npc.type(),
                        npc.position(),
                        newSkin,
                        npc.profession(),
                        npc.entityType(),
                        npc.holograms(),
                        npc.looking(),
                        npc.hologramYOffset()
                );
                updatedNpcs.add(updatedNpc);
                found = true;
            } else {
                updatedNpcs.add(npc);
            }
        }

        if (!found) {
            Logger.error("Cannot update NPC '{}' skin - not found in config", npcId);
            return false;
        }

        // Save back to file
        NPCJsonConfig updatedConfig = new NPCJsonConfig(updatedNpcs);
        loadedConfigs.put(filePath, updatedConfig);

        return saveToFile(filePath, updatedConfig);
    }
}
