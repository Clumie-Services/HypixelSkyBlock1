package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.chocolatefactory.HoppityCollectionData;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Datapoint for storing and persisting Hoppity rabbit collection data.
 */
public class DatapointHoppityCollection extends SkyBlockDatapoint<HoppityCollectionData> {

    private static final Serializer<HoppityCollectionData> serializer = new Serializer<>() {

        @Override
        public String serialize(HoppityCollectionData value) {
            JSONObject json = new JSONObject();

            // Collected rabbit IDs
            JSONArray rabbitsArray = new JSONArray();
            for (String rabbitId : value.getCollectedRabbitIds()) {
                rabbitsArray.put(rabbitId);
            }
            json.put("collectedRabbits", rabbitsArray);

            // Rabbit duplicates
            JSONObject duplicates = new JSONObject();
            for (Map.Entry<String, Integer> entry : value.getRabbitDuplicates().entrySet()) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
            json.put("rabbitDuplicates", duplicates);

            // Statistics
            json.put("totalRabbitsFound", value.getTotalRabbitsFound());
            json.put("eggsFound", value.getEggsFound());
            json.put("strayRabbitsFound", value.getStrayRabbitsFound());
            json.put("goldenRabbitsFound", value.getGoldenRabbitsFound());

            return json.toString();
        }

        @Override
        public HoppityCollectionData deserialize(String json) {
            HoppityCollectionData data = new HoppityCollectionData();

            if (json == null || json.isEmpty()) {
                return data;
            }

            try {
                JSONObject jsonObject = new JSONObject(json);

                // Collected rabbit IDs
                if (jsonObject.has("collectedRabbits")) {
                    JSONArray rabbitsArray = jsonObject.getJSONArray("collectedRabbits");
                    Set<String> rabbits = new HashSet<>();
                    for (int i = 0; i < rabbitsArray.length(); i++) {
                        rabbits.add(rabbitsArray.getString(i).toUpperCase());
                    }
                    data.setCollectedRabbitIds(rabbits);
                }

                // Rabbit duplicates
                if (jsonObject.has("rabbitDuplicates")) {
                    JSONObject duplicatesJson = jsonObject.getJSONObject("rabbitDuplicates");
                    Map<String, Integer> duplicates = new HashMap<>();
                    for (String key : duplicatesJson.keySet()) {
                        duplicates.put(key.toUpperCase(), duplicatesJson.getInt(key));
                    }
                    data.setRabbitDuplicates(duplicates);
                }

                // Statistics
                data.setTotalRabbitsFound(jsonObject.optInt("totalRabbitsFound", 0));
                data.setEggsFound(jsonObject.optInt("eggsFound", 0));
                data.setStrayRabbitsFound(jsonObject.optInt("strayRabbitsFound", 0));
                data.setGoldenRabbitsFound(jsonObject.optInt("goldenRabbitsFound", 0));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        public HoppityCollectionData clone(HoppityCollectionData value) {
            return value.copy();
        }
    };

    public DatapointHoppityCollection(String key, HoppityCollectionData value) {
        super(key, value, serializer);
    }

    public DatapointHoppityCollection(String key) {
        super(key, new HoppityCollectionData(), serializer);
    }
}
