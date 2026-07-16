package vg.civcraft.mc.civchat2.prefix;

import com.programmerdan.minecraft.banstick.data.BSPlayer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.CivChat2;

public class StarManager {

    private static final String STAR = "⋆";
    private static final String MOON = "☾";
    private static final String MOON_PREFIX_PERMISSION = "civchat.prefix.moon";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu");

    private final boolean playtimeStars;

    public StarManager(boolean playtimeStars) {
        this.playtimeStars = playtimeStars;
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

    public HoverEventSource<?> hover(Player player) {
        long firstPlayed = getJoined(player);
        String joined = firstPlayed == 0 ? "unknown" : FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(firstPlayed), ZoneId.systemDefault()));

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

        String text = "Rank: " + rank + "\nJoined: " + joined;
        if (player.hasPermission("rankedpvpstar")) {
            text += "\nTop 10 in ranked PvP";
        }

        return HoverEvent.showText(Component.text(text));
    }

    public String getPrefix(Player player) {
        if (!CivChat2.getInstance().getCivChat2SettingsManager().isShowPrefix(player.getUniqueId())) {
            return "";
        }

        final String prefix;
        if (player.hasPermission("civchat.admin")) {
            return ChatColor.DARK_RED + STAR;
        } else if (player.hasPermission("civchat.superfriend")) {
            prefix = ChatColor.RED + STAR.repeat(2);
        } else if (player.hasPermission("civchat.mod")) {
            prefix = ChatColor.RED + STAR;
        } else {
            prefix = "";
        }

        final boolean staff = player.hasPermission("civchat.superfriend") || player.hasPermission("civchat.mod");
        final int purpleStars = player.hasPermission("rankedpvpstar") ? 1 : 0;

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

        final StringBuilder rendered = new StringBuilder(prefix);

        if (!staff && playtimeStars
            && CivChat2.getInstance().getCivChat2SettingsManager().isShowStars(player.getUniqueId())) {
            long firstPlayed = getJoined(player);
            int yellowStars = firstPlayed == 0 ? 0 : (int) LocalDateTime.ofInstant(Instant.ofEpochMilli(firstPlayed), ZoneId.systemDefault()).until(LocalDateTime.now(), ChronoUnit.YEARS);
            yellowStars = Math.max(0, yellowStars - greenStars - purpleStars);
            if (yellowStars > 0) {
                rendered.append(ChatColor.YELLOW).append(STAR.repeat(yellowStars));
            }
        }
        if (!staff && greenStars > 0
            && CivChat2.getInstance().getCivChat2SettingsManager().isShowPatreonPrefix(player.getUniqueId())) {
            rendered.append(ChatColor.GREEN).append(STAR.repeat(greenStars));
        }
        if (player.hasPermission(MOON_PREFIX_PERMISSION)
            && CivChat2.getInstance().getCivChat2SettingsManager().isShowCustomPrefixes(player.getUniqueId())) {
            rendered.append(ChatColor.GOLD).append(MOON);
        }
        if (!staff && purpleStars > 0
            && CivChat2.getInstance().getCivChat2SettingsManager().isShowPvpStarPrefix(player.getUniqueId())) {
            rendered.append(ChatColor.LIGHT_PURPLE).append(STAR.repeat(purpleStars));
        }

        return rendered.toString();
    }
}
