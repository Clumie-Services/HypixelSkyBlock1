package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateFactoryData;
import net.swofty.type.skyblockgeneric.chocolatefactory.employee.ChocolateEmployee;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Datapoint for storing and persisting Chocolate Factory data.
 */
public class DatapointChocolateFactory extends SkyBlockDatapoint<ChocolateFactoryData> {

    private static final Serializer<ChocolateFactoryData> serializer = new Serializer<>() {

        @Override
        public String serialize(ChocolateFactoryData value) {
            JSONObject json = new JSONObject();

            // Core values
            json.put("chocolate", value.getChocolate());
            json.put("allTimeChocolate", value.getAllTimeChocolate());
            json.put("prestigeChocolate", value.getPrestigeChocolate());
            json.put("shopSpent", value.getShopSpent());

            // Factory level
            json.put("factoryLevel", value.getFactoryLevel());

            // Employee levels
            JSONObject employees = new JSONObject();
            for (ChocolateEmployee employee : ChocolateEmployee.values()) {
                employees.put(employee.name(), value.getEmployeeLevel(employee));
            }
            json.put("employeeLevels", employees);

            // Upgrades
            json.put("rabbitBarnLevel", value.getRabbitBarnLevel());
            json.put("handBakedLevel", value.getHandBakedLevel());

            // Time Tower
            json.put("timeTowerLevel", value.getTimeTowerLevel());
            json.put("timeTowerCharges", value.getTimeTowerCharges());
            json.put("timeTowerActivatedAt", value.getTimeTowerActivatedAt());
            json.put("timeTowerLastChargeAt", value.getTimeTowerLastChargeAt());

            // Other upgrades
            json.put("rabbitShrineLevel", value.getRabbitShrineLevel());
            json.put("coachJackrabbitLevel", value.getCoachJackrabbitLevel());

            // Misc
            json.put("elDoradoFinds", value.getElDoradoFinds());
            json.put("lastClickTime", value.getLastClickTime());
            json.put("rabbitHitmanSlots", value.getRabbitHitmanSlots());

            return json.toString();
        }

        @Override
        public ChocolateFactoryData deserialize(String json) {
            ChocolateFactoryData data = new ChocolateFactoryData();

            if (json == null || json.isEmpty()) {
                return data;
            }

            try {
                JSONObject jsonObject = new JSONObject(json);

                // Core values
                data.setChocolate(jsonObject.optLong("chocolate", 0));
                data.setAllTimeChocolate(jsonObject.optLong("allTimeChocolate", 0));
                data.setPrestigeChocolate(jsonObject.optLong("prestigeChocolate", 0));
                data.setShopSpent(jsonObject.optLong("shopSpent", 0));

                // Factory level
                data.setFactoryLevel(jsonObject.optInt("factoryLevel", 1));

                // Employee levels
                if (jsonObject.has("employeeLevels")) {
                    JSONObject employees = jsonObject.getJSONObject("employeeLevels");
                    for (ChocolateEmployee employee : ChocolateEmployee.values()) {
                        int level = employees.optInt(employee.name(), 0);
                        data.setEmployeeLevel(employee, level);
                    }
                }

                // Upgrades
                data.setRabbitBarnLevel(jsonObject.optInt("rabbitBarnLevel", 0));
                data.setHandBakedLevel(jsonObject.optInt("handBakedLevel", 0));

                // Time Tower
                data.setTimeTowerLevel(jsonObject.optInt("timeTowerLevel", 0));
                data.setTimeTowerCharges(jsonObject.optInt("timeTowerCharges", 0));
                data.setTimeTowerActivatedAt(jsonObject.optLong("timeTowerActivatedAt", -1));
                data.setTimeTowerLastChargeAt(jsonObject.optLong("timeTowerLastChargeAt", System.currentTimeMillis()));

                // Other upgrades
                data.setRabbitShrineLevel(jsonObject.optInt("rabbitShrineLevel", 0));
                data.setCoachJackrabbitLevel(jsonObject.optInt("coachJackrabbitLevel", 0));

                // Misc
                data.setElDoradoFinds(jsonObject.optInt("elDoradoFinds", 0));
                data.setLastClickTime(jsonObject.optLong("lastClickTime", 0));
                data.setRabbitHitmanSlots(jsonObject.optInt("rabbitHitmanSlots", 0));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        public ChocolateFactoryData clone(ChocolateFactoryData value) {
            return value.copy();
        }
    };

    public DatapointChocolateFactory(String key, ChocolateFactoryData value) {
        super(key, value, serializer);
    }

    public DatapointChocolateFactory(String key) {
        super(key, new ChocolateFactoryData(), serializer);
    }
}
