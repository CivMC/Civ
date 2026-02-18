package vg.civcraft.mc.civchat2.prefix;

import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.CivChat2;

public class StarManager {
    private static final String STAR = "⋆";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu");
    private final boolean playtimeStars;
    private final Gson gson = new GsonBuilder().create();
    private final File storageFile;

    public StarManager(boolean playtimeStars) {
    this.playtimeStars = playtimeStars;
    
    File civproxyDir = new File("/shared-data");
    this.storageFile = new File(civproxyDir, "display_names.json");
    
    CivChat2.getInstance().getLogger().info("StarManager storage file: " + storageFile.getAbsolutePath());
}

    private long getJoined(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("BanStick")) {
            return player.getFirstPlayed();
        }

        BSPlayer bsPlayer = BSPlayer.byUUID(player.getUniqueId());
        if (bsPlayer == null) {
            return 0;
        }
        return bsPlayer.getFirstAdd().toInstant().toEpochMilli();
    }

    private List<String> getDisplayNameHistory(UUID uuid) {
        
        if (!storageFile.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(storageFile)) {
            Map<String, List<String>> displayNames = gson.fromJson(reader, new TypeToken<Map<String, List<String>>>(){}.getType());
            
            if (displayNames == null) {
                return new ArrayList<>();
            }
            
            List<String> result = displayNames.getOrDefault(uuid.toString(), new ArrayList<>());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public HoverEventSource<?> hover(Player player) {
        long firstPlayed = getJoined(player);
        String joined = firstPlayed == 0 ? "unknown" : FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(firstPlayed), ZoneId.systemDefault()));

        // Get display name history from file
        List<String> nameHistory = getDisplayNameHistory(player.getUniqueId());
        String displayNameLine = "";
        if (nameHistory != null && !nameHistory.isEmpty()) {
            if (nameHistory.size() == 1) {
                displayNameLine = "\nOriginal Name: " + nameHistory.get(0);
            } else {
                displayNameLine = "\nName History: " + String.join(" → ", nameHistory);
            }
        }

        String rank;
        if (player.hasPermission("civchat.admin")) {
            rank = "Admin";
        } else if (player.hasPermission("civchat.superfriend")) {
            rank = "Superfriend";
        } else if (player.hasPermission("civchat.mod")) {
            rank = "Moderator";
        } else if (player.hasPermission("civchat.powerplayer")) {
            rank = "Civ Power Player";
        } else if (player.hasPermission("civchat.grinder")) {
            rank = "Civ Grinder";
        } else if (player.hasPermission("civchat.bigdonator")) {
            rank = "Loyal Patron";
        } else if (player.hasPermission("civchat.donator")) {
            rank = "Namecolor Patron";
        } else {
            rank = "None";
        }

        return HoverEvent.showText(Component.text("Rank: " + rank + "\nJoined: " + joined + displayNameLine));
    }

    public String getPrefix(Player player) {
        if (!CivChat2.getInstance().getCivChat2SettingsManager().isShowPrefix(player.getUniqueId())) {
            return "";
        }

        if (player.hasPermission("civchat.admin")) {
            return ChatColor.DARK_RED + STAR;
        } else if (player.hasPermission("civchat.superfriend")) {
            return ChatColor.RED + STAR + STAR;
        } else if (player.hasPermission("civchat.mod")) {
            return ChatColor.RED + STAR;
        }

        int greenStars = 0;
        if (player.hasPermission("civchat.powerplayer")) {
            greenStars = 4;
        } else if (player.hasPermission("civchat.grinder")) {
            greenStars = 3;
        } else if (player.hasPermission("civchat.bigdonator")) {
            greenStars = 2;
        } else if (player.hasPermission("civchat.donator")) {
            greenStars = 1;
        }

        StringBuilder stars = new StringBuilder();
        if (playtimeStars) {
            long firstPlayed = getJoined(player);
            int yellowStars = firstPlayed == 0 ? 0 : (int) LocalDateTime.ofInstant(Instant.ofEpochMilli(firstPlayed), ZoneId.systemDefault()).until(LocalDateTime.now(), ChronoUnit.YEARS);
            yellowStars = Math.max(0, yellowStars - greenStars);
            if (yellowStars > 0) {
                stars.append(ChatColor.YELLOW);
            }
            stars.append(STAR.repeat(yellowStars));
        }
        if (greenStars > 0) {
            stars.append(ChatColor.GREEN);
        }
        stars.append(STAR.repeat(greenStars));

        return stars.toString();
    }
}