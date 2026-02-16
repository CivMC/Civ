package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.common.base.Strings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhoIs extends BaseCommand {
    private final Gson gson = new GsonBuilder().create();

    @CommandAlias("whois")
    @Syntax("<player name>")
    @Description("Tells you who someone is (and previous usernames)")
    public void execute(CommandSender sender, String playerName) {
        // Try to find the player by name
        Player targetPlayer = Bukkit.getPlayer(playerName);
        UUID targetUUID = null;
        
        if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
        } else {
            // If player not online, try to get UUID from NameLayerAPI
            targetUUID = NameLayerAPI.getUUID(playerName);
        }
        
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }
        
        final String name = NameLayerAPI.getCurrentName(targetUUID);
        
        if (!Strings.isNullOrEmpty(name)) {
            String message = ChatColor.YELLOW + playerName + " is: " + ChatColor.RESET + name;
            
            // Get previous usernames
            List<String> previousNames = getDisplayNameHistory(targetUUID);
            if (!previousNames.isEmpty()) {
                message += ChatColor.YELLOW + "\nPrevious names: " + ChatColor.RESET + String.join(" → ", previousNames);
            }
            
            sender.sendMessage(message);
            return;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "They are: " + ChatColor.RESET + playerName);
    }

    private List<String> getDisplayNameHistory(UUID uuid) {
        File storageFile = new File("/shared-data/display_names.json");
        
        if (!storageFile.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(storageFile)) {
            Map<String, List<String>> displayNames = gson.fromJson(reader, new TypeToken<Map<String, List<String>>>(){}.getType());
            
            if (displayNames == null) {
                return new ArrayList<>();
            }
            
            return displayNames.getOrDefault(uuid.toString(), new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}