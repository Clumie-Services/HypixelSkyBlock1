package net.swofty.type.generic.command.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.json.JsonConfiguredNPC;
import net.swofty.type.generic.entity.npc.json.NPCJsonConfig;
import net.swofty.type.generic.entity.npc.json.NPCJsonLoader;
import net.swofty.type.generic.entity.npc.json.NPCSelectionManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import org.tinylog.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@CommandParameters(
        description = "NPC management commands",
        usage = "/npc <sel|desel|info|move|skin-url|export|list>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class NPCCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        // /npc sel - Select the NPC you're looking at
        ArgumentLiteral selArg = ArgumentType.Literal("sel");

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;

            // Find NPC player is looking at (any NPC, not just JSON ones)
            HypixelNPC npc = NPCSelectionManager.getNPCLookingAt(player, 10);

            if (npc == null) {
                player.sendMessage("§cNo NPC found in your line of sight.");
                player.sendMessage("§7Make sure you're looking at an NPC within 10 blocks.");
                return;
            }

            // Select the NPC
            NPCSelectionManager.selectNPC(player, npc);

            // Give wand (only useful for JSON NPCs, but give it anyway for convenience)
            player.getInventory().addItemStack(NPCSelectionManager.createWand());

            String npcId = npc instanceof JsonConfiguredNPC jsonNpc ? jsonNpc.getId() : npc.getClass().getSimpleName();
            boolean isJsonNpc = npc instanceof JsonConfiguredNPC;

            player.sendMessage("§aSelected NPC: §e" + npc.getName() + " §7(" + npcId + ")");
            if (isJsonNpc) {
                player.sendMessage("§7Right-click on a block with the §6NPC Position Wand §7to move the NPC.");
            } else {
                player.sendMessage("§7This is a §cJava-coded NPC§7. Use §e/npc export <file> §7to convert it to JSON.");
            }
        }, selArg);

        // /npc desel - Deselect current NPC
        ArgumentLiteral deselArg = ArgumentType.Literal("desel");

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;

            if (!NPCSelectionManager.hasSelection(player)) {
                player.sendMessage("§cYou don't have an NPC selected.");
                return;
            }

            NPCSelectionManager.clearSelection(player);
            player.sendMessage("§aDeselected NPC.");
        }, deselArg);

        // /npc info - Show info about selected NPC
        ArgumentLiteral infoArg = ArgumentType.Literal("info");

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;

            HypixelNPC npc = NPCSelectionManager.getSelectedNPC(player);
            if (npc == null) {
                player.sendMessage("§cYou don't have an NPC selected. Use §e/npc sel §cwhile looking at an NPC.");
                return;
            }

            var pos = npc.getParameters().position(player);
            boolean isJsonNpc = npc instanceof JsonConfiguredNPC;
            String npcId = isJsonNpc ? ((JsonConfiguredNPC) npc).getId() : npc.getClass().getSimpleName();

            player.sendMessage("§6=== NPC Info ===");
            player.sendMessage("§7ID: §e" + npcId);
            player.sendMessage("§7Name: §e" + npc.getName());
            player.sendMessage("§7Class: §e" + npc.getClass().getSimpleName());
            player.sendMessage("§7Type: §e" + (isJsonNpc ? "JSON-configured" : "Java-coded"));
            player.sendMessage("§7Position: §e" + String.format("%.2f, %.2f, %.2f", pos.x(), pos.y(), pos.z()));
            player.sendMessage("§7Yaw/Pitch: §e" + String.format("%.1f, %.1f", pos.yaw(), pos.pitch()));
            if (isJsonNpc) {
                String sourceFile = NPCJsonLoader.getSourceFile(((JsonConfiguredNPC) npc).getId());
                player.sendMessage("§7Source File: §e" + (sourceFile != null ? sourceFile : "Unknown"));
            }
        }, infoArg);

        // /npc export <file> - Export the selected NPC to a JSON file
        ArgumentLiteral exportArg = ArgumentType.Literal("export");
        ArgumentString fileArg = ArgumentType.String("file");

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;

            HypixelNPC npc = NPCSelectionManager.getSelectedNPC(player);
            if (npc == null) {
                player.sendMessage("§cYou don't have an NPC selected. Use §e/npc sel §cwhile looking at an NPC.");
                return;
            }

            String filePath = context.get(fileArg);
            // Ensure .json extension
            if (!filePath.endsWith(".json")) {
                filePath += ".json";
            }
            // Default to configuration folder if no path specified
            if (!filePath.contains("/")) {
                filePath = "configuration/" + filePath;
            }

            // Generate ID from class name
            String npcId = NPCJsonLoader.generateNPCId(npc);

            // Check if this NPC is already JSON configured
            if (npc instanceof JsonConfiguredNPC jsonNpc) {
                npcId = jsonNpc.getId();
                player.sendMessage("§eThis NPC is already JSON-configured. Re-exporting with current values...");
            }

            boolean success = NPCJsonLoader.exportNPCToFile(npc, npcId, filePath, player);
            if (success) {
                player.sendMessage("§aSuccessfully exported NPC to: §e" + filePath);
                player.sendMessage("§7NPC ID: §e" + npcId);

                // If this was a Java NPC, create a JsonConfiguredNPC and update the selection
                // so the user can immediately move/edit it without restarting
                if (!(npc instanceof JsonConfiguredNPC)) {
                    NPCJsonConfig.NPCData exportedData = NPCJsonLoader.exportNPCToData(npc, npcId, player);
                    JsonConfiguredNPC jsonNpc = new JsonConfiguredNPC(exportedData);

                    // Replace selection with the new JSON-configured NPC
                    NPCSelectionManager.selectNPC(player, jsonNpc);

                    player.sendMessage("§7You can now move and edit this NPC immediately.");
                }
            } else {
                player.sendMessage("§cFailed to export NPC. Check console for errors.");
            }
        }, exportArg, fileArg);

        // /npc list - List all registered NPCs
        ArgumentLiteral listArg = ArgumentType.Literal("list");

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;

            var npcs = HypixelNPC.getRegisteredNPCs();
            player.sendMessage("§6=== Registered NPCs (" + npcs.size() + ") ===");

            int jsonCount = 0;
            int javaCount = 0;

            for (HypixelNPC npc : npcs) {
                boolean isJson = npc instanceof JsonConfiguredNPC;
                if (isJson) jsonCount++;
                else javaCount++;

                String id = isJson ? ((JsonConfiguredNPC) npc).getId() : npc.getClass().getSimpleName();
                String typeColor = isJson ? "§a" : "§c";
                String typeLabel = isJson ? "[JSON]" : "[Java]";
                player.sendMessage(typeColor + typeLabel + " §e" + npc.getName() + " §7(" + id + ")");
            }

            player.sendMessage("§7Total: §e" + npcs.size() + " §7(§a" + jsonCount + " JSON§7, §c" + javaCount + " Java§7)");
        }, listArg);

        // /npc move - Move the selected NPC to your location
        ArgumentLiteral moveArg = ArgumentType.Literal("move");

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;

            HypixelNPC npc = NPCSelectionManager.getSelectedNPC(player);
            if (npc == null) {
                player.sendMessage("§cYou don't have an NPC selected. Use §e/npc sel §cwhile looking at an NPC.");
                return;
            }

            // Check if NPC is JSON-configured
            if (!(npc instanceof JsonConfiguredNPC)) {
                player.sendMessage("§cThis NPC is not configured via JSON and cannot be moved.");
                player.sendMessage("§7Use §e/npc export <file> §7to convert it to JSON first.");
                return;
            }

            // Get player's current position with their yaw (facing direction)
            Pos playerPos = player.getPosition();
            Pos npcPos = new Pos(
                    playerPos.x(),
                    playerPos.y(),
                    playerPos.z(),
                    playerPos.yaw(),
                    0 // NPCs typically don't have pitch
            );

            // Move the NPC
            boolean success = NPCSelectionManager.moveSelectedNPC(player, npcPos);

            if (success) {
                player.sendMessage("§aMoved NPC §e" + npc.getName() + " §ato your location.");
                player.sendMessage("§7Position: §e" + String.format("%.2f, %.2f, %.2f", npcPos.x(), npcPos.y(), npcPos.z()));
                player.sendMessage("§7Yaw: §e" + String.format("%.1f", npcPos.yaw()));
            } else {
                player.sendMessage("§cFailed to move NPC. Check console for errors.");
            }
        }, moveArg);

        // /npc skin-url <url> - Set NPC skin from a mineskin.org URL
        ArgumentLiteral skinUrlArg = ArgumentType.Literal("skin-url");
        ArgumentString urlArg = ArgumentType.String("url");

        command.addSyntax((sender, context) -> {
            HypixelPlayer player = (HypixelPlayer) sender;

            HypixelNPC npc = NPCSelectionManager.getSelectedNPC(player);
            if (npc == null) {
                player.sendMessage("§cYou don't have an NPC selected. Use §e/npc sel §cwhile looking at an NPC.");
                return;
            }

            // Check if NPC is JSON-configured
            if (!(npc instanceof JsonConfiguredNPC jsonNpc)) {
                player.sendMessage("§cThis NPC is not configured via JSON and cannot have its skin changed.");
                player.sendMessage("§7Use §e/npc export <file> §7to convert it to JSON first.");
                return;
            }

            // Check if NPC is a human type
            if (!(npc.getParameters() instanceof HumanConfiguration)) {
                player.sendMessage("§cThis NPC is not a human NPC. Only human NPCs can have skins.");
                return;
            }

            String url = context.get(urlArg);
            player.sendMessage("§7Fetching skin from URL...");

            // Fetch skin data asynchronously
            Thread.startVirtualThread(() -> {
                try {
                    SkinData skinData = fetchSkinFromUrl(url);
                    if (skinData == null) {
                        player.sendMessage("§cFailed to fetch skin data from URL.");
                        player.sendMessage("§7Make sure the URL is a valid mineskin.org API URL.");
                        player.sendMessage("§7Example: §ehttps://api.mineskin.org/get/uuid/xxxxx");
                        return;
                    }

                    // Update the NPC skin
                    boolean success = NPCJsonLoader.updateNPCSkin(jsonNpc.getId(), skinData.texture, skinData.signature);

                    if (success) {
                        // Clear NPC from player caches to force reload with new skin
                        NPCSelectionManager.clearNPCFromAllCaches(npc);

                        player.sendMessage("§aSuccessfully updated skin for NPC §e" + npc.getName() + "§a!");
                        player.sendMessage("§7The NPC will respawn with the new skin shortly.");
                    } else {
                        player.sendMessage("§cFailed to save skin data. Check console for errors.");
                    }
                } catch (Exception e) {
                    Logger.error("Failed to fetch skin from URL: {}", url, e);
                    player.sendMessage("§cError fetching skin: " + e.getMessage());
                }
            });
        }, skinUrlArg, urlArg);
    }

    /**
     * Fetches skin data from a mineskin.org URL or similar API.
     */
    private static SkinData fetchSkinFromUrl(String url) {
        try {
            // Convert mineskin.org page URLs to API URLs
            if (url.contains("mineskin.org/skins/") || url.contains("mineskin.org/skin/")) {
                // Extract the ID from the URL
                String id = url.replaceAll(".*/skins?/", "").split("[?#]")[0];
                url = "https://api.mineskin.org/get/id/" + id;
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "HypixelSkyBlock-NPC-Manager")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                Logger.error("Failed to fetch skin: HTTP {}", response.statusCode());
                return null;
            }

            String body = response.body();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            // Try different JSON structures (mineskin API format)
            String texture = null;
            String signature = null;

            if (json.has("data")) {
                JsonObject data = json.getAsJsonObject("data");
                if (data.has("texture")) {
                    JsonObject textureObj = data.getAsJsonObject("texture");
                    texture = textureObj.get("value").getAsString();
                    signature = textureObj.get("signature").getAsString();
                }
            }

            // Alternative structure
            if (texture == null && json.has("texture")) {
                JsonObject textureObj = json.getAsJsonObject("texture");
                texture = textureObj.get("value").getAsString();
                signature = textureObj.get("signature").getAsString();
            }

            // Direct value/signature at root
            if (texture == null && json.has("value") && json.has("signature")) {
                texture = json.get("value").getAsString();
                signature = json.get("signature").getAsString();
            }

            if (texture != null && signature != null) {
                return new SkinData(texture, signature);
            }

            Logger.error("Could not find texture/signature in response: {}", body);
            return null;
        } catch (Exception e) {
            Logger.error("Error fetching skin from URL", e);
            return null;
        }
    }

    private record SkinData(String texture, String signature) {}
}
