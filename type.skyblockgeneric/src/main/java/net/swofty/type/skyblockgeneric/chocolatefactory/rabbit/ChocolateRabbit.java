package net.swofty.type.skyblockgeneric.chocolatefactory.rabbit;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Registry for all chocolate rabbits in the game.
 * Rabbits are loaded from configuration/chocolatefactory/rabbits.json
 */
@Getter
public class ChocolateRabbit {
    private static final Map<String, ChocolateRabbit> RABBITS = new LinkedHashMap<>();
    private static final Map<ChocolateRabbitRarity, List<ChocolateRabbit>> RABBITS_BY_RARITY = new EnumMap<>(ChocolateRabbitRarity.class);
    private static boolean initialized = false;

    private final String id;
    private final String name;
    private final ChocolateRabbitRarity rarity;
    private final String residentIsland;
    private final RabbitRequirement requirement;
    private final String skinTexture;
    private final String description;

    public ChocolateRabbit(String id, String name, ChocolateRabbitRarity rarity,
                          String residentIsland, RabbitRequirement requirement,
                          String skinTexture, String description) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.residentIsland = residentIsland;
        this.requirement = requirement;
        this.skinTexture = skinTexture;
        this.description = description;
    }

    /**
     * Gets a rabbit by its ID
     */
    public static ChocolateRabbit getById(String id) {
        ensureInitialized();
        return RABBITS.get(id.toUpperCase());
    }

    /**
     * Gets all registered rabbits
     */
    public static Collection<ChocolateRabbit> getAllRabbits() {
        ensureInitialized();
        return Collections.unmodifiableCollection(RABBITS.values());
    }

    /**
     * Gets all rabbits of a specific rarity
     */
    public static List<ChocolateRabbit> getRabbitsByRarity(ChocolateRabbitRarity rarity) {
        ensureInitialized();
        return RABBITS_BY_RARITY.getOrDefault(rarity, Collections.emptyList());
    }

    /**
     * Gets the total number of rabbits
     */
    public static int getTotalRabbitCount() {
        ensureInitialized();
        return RABBITS.size();
    }

    /**
     * Gets the number of rabbits of a specific rarity
     */
    public static int getRabbitCountByRarity(ChocolateRabbitRarity rarity) {
        ensureInitialized();
        return RABBITS_BY_RARITY.getOrDefault(rarity, Collections.emptyList()).size();
    }

    /**
     * Selects a random rabbit based on rarity weights
     */
    public static ChocolateRabbit getRandomRabbit(Random random) {
        ensureInitialized();

        // Build weighted list
        List<ChocolateRabbit> weightedList = new ArrayList<>();
        for (ChocolateRabbit rabbit : RABBITS.values()) {
            int weight = rabbit.getRarity().getSpawnWeight();
            for (int i = 0; i < weight; i++) {
                weightedList.add(rabbit);
            }
        }

        if (weightedList.isEmpty()) return null;
        return weightedList.get(random.nextInt(weightedList.size()));
    }

    /**
     * Selects a random rabbit of at least a given rarity
     */
    public static ChocolateRabbit getRandomRabbitOfRarity(ChocolateRabbitRarity minRarity, Random random) {
        ensureInitialized();

        List<ChocolateRabbit> eligible = new ArrayList<>();
        for (ChocolateRabbit rabbit : RABBITS.values()) {
            if (rabbit.getRarity().isAtLeast(minRarity)) {
                int weight = rabbit.getRarity().getSpawnWeight();
                for (int i = 0; i < weight; i++) {
                    eligible.add(rabbit);
                }
            }
        }

        if (eligible.isEmpty()) return null;
        return eligible.get(random.nextInt(eligible.size()));
    }

    /**
     * Initializes the rabbit registry from configuration file
     */
    public static void initialize(Path configPath) {
        if (initialized) return;

        // Initialize rarity lists
        for (ChocolateRabbitRarity rarity : ChocolateRabbitRarity.values()) {
            RABBITS_BY_RARITY.put(rarity, new ArrayList<>());
        }

        try {
            String content = Files.readString(configPath);
            JSONObject json = new JSONObject(content);
            JSONArray rabbitsArray = json.getJSONArray("rabbits");

            for (int i = 0; i < rabbitsArray.length(); i++) {
                JSONObject rabbitJson = rabbitsArray.getJSONObject(i);
                ChocolateRabbit rabbit = fromJson(rabbitJson);
                registerRabbit(rabbit);
            }

            initialized = true;
            System.out.println("[ChocolateFactory] Loaded " + RABBITS.size() + " rabbits from configuration");
        } catch (IOException e) {
            System.err.println("[ChocolateFactory] Failed to load rabbits.json: " + e.getMessage());
            // Initialize with default rabbits
            initializeDefaultRabbits();
            initialized = true;
        }
    }

    /**
     * Creates a rabbit from JSON data
     */
    private static ChocolateRabbit fromJson(JSONObject json) {
        String id = json.getString("id");
        String name = json.getString("name");
        ChocolateRabbitRarity rarity = ChocolateRabbitRarity.fromString(json.getString("rarity"));
        String residentIsland = json.optString("residentIsland", null);
        RabbitRequirement requirement = RabbitRequirement.fromJson(json.optJSONObject("requirement"));
        String skinTexture = json.optString("skinTexture", getDefaultSkinTexture());
        String description = json.optString("description", "A chocolate rabbit.");

        return new ChocolateRabbit(id, name, rarity, residentIsland, requirement, skinTexture, description);
    }

    /**
     * Registers a rabbit in the registry
     */
    private static void registerRabbit(ChocolateRabbit rabbit) {
        RABBITS.put(rabbit.getId().toUpperCase(), rabbit);
        RABBITS_BY_RARITY.get(rabbit.getRarity()).add(rabbit);
    }

    /**
     * Ensures the registry is initialized
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initializeDefaultRabbits();
            initialized = true;
        }
    }

    /**
     * Initializes default rabbits if config file is not available
     */
    private static void initializeDefaultRabbits() {
        // Initialize rarity lists
        for (ChocolateRabbitRarity rarity : ChocolateRabbitRarity.values()) {
            RABBITS_BY_RARITY.put(rarity, new ArrayList<>());
        }

        // Common rabbits
        registerDefaultRabbit("AARON", "Aaron", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("ALEX", "Alex", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("AMY", "Amy", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("ANDREW", "Andrew", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("ANNA", "Anna", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("BEN", "Ben", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("BETTY", "Betty", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("BOB", "Bob", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("CARL", "Carl", ChocolateRabbitRarity.COMMON);
        registerDefaultRabbit("CARLA", "Carla", ChocolateRabbitRarity.COMMON);

        // Uncommon rabbits
        registerDefaultRabbit("CHARLES", "Charles", ChocolateRabbitRarity.UNCOMMON);
        registerDefaultRabbit("CHARLOTTE", "Charlotte", ChocolateRabbitRarity.UNCOMMON);
        registerDefaultRabbit("CHRIS", "Chris", ChocolateRabbitRarity.UNCOMMON);
        registerDefaultRabbit("CLAIRE", "Claire", ChocolateRabbitRarity.UNCOMMON);
        registerDefaultRabbit("DAVID", "David", ChocolateRabbitRarity.UNCOMMON);

        // Rare rabbits
        registerDefaultRabbit("DANTE", "Dante", ChocolateRabbitRarity.RARE);
        registerDefaultRabbit("DIEGO", "Diego", ChocolateRabbitRarity.RARE);
        registerDefaultRabbit("DOROTHY", "Dorothy", ChocolateRabbitRarity.RARE);
        registerDefaultRabbit("EDWARD", "Edward", ChocolateRabbitRarity.RARE);
        registerDefaultRabbit("ELEANOR", "Eleanor", ChocolateRabbitRarity.RARE);

        // Epic rabbits
        registerDefaultRabbit("EINSTEIN", "Einstein", ChocolateRabbitRarity.EPIC);
        registerDefaultRabbit("EVELYN", "Evelyn", ChocolateRabbitRarity.EPIC);
        registerDefaultRabbit("FELIX", "Felix", ChocolateRabbitRarity.EPIC);
        registerDefaultRabbit("FLORA", "Flora", ChocolateRabbitRarity.EPIC);

        // Legendary rabbits
        registerDefaultRabbit("ZORRO", "Zorro", ChocolateRabbitRarity.LEGENDARY);
        registerDefaultRabbit("ZEUS", "Zeus", ChocolateRabbitRarity.LEGENDARY);
        registerDefaultRabbit("COCOA", "Cocoa", ChocolateRabbitRarity.LEGENDARY);

        // Mythic rabbits
        registerDefaultRabbit("HOPPITY", "Hoppity", ChocolateRabbitRarity.MYTHIC);
        registerDefaultRabbit("MU", "Mu", ChocolateRabbitRarity.MYTHIC);

        // Divine rabbits
        registerDefaultRabbit("EL_DORADO", "El Dorado", ChocolateRabbitRarity.DIVINE);
    }

    private static void registerDefaultRabbit(String id, String name, ChocolateRabbitRarity rarity) {
        ChocolateRabbit rabbit = new ChocolateRabbit(
                id, name, rarity, null,
                new RabbitRequirement(RabbitRequirement.RequirementType.NONE),
                getDefaultSkinTexture(),
                "A " + rarity.getDisplayName().toLowerCase() + " chocolate rabbit."
        );
        registerRabbit(rabbit);
    }

    private static String getDefaultSkinTexture() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBjZWZhNjdlNzU2NDJkMjY5MzM4MzY5ZTJmNjJlNzY3ZDQ5NDVjZTcyMjNjNmQyZTZlMGYxYTFhNjdkNjg0YyJ9fX0=";
    }

    /**
     * Gets the formatted display name with rarity color
     */
    public String getFormattedName() {
        return rarity.getColorCode() + name;
    }

    /**
     * Gets the base CpS this rabbit provides
     */
    public int getBaseCps() {
        return rarity.getBaseCps();
    }

    /**
     * Gets the multiplier bonus this rabbit provides
     */
    public double getMultiplierBonus() {
        return rarity.getMultiplierBonus();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChocolateRabbit that = (ChocolateRabbit) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
