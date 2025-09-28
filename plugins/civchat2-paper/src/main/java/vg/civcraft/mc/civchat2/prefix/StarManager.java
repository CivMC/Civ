package vg.civcraft.mc.civchat2.prefix;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.CivChat2;

public class StarManager {

    private static final String STAR = "⋆";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu");

    public HoverEventSource<?> hover(Player player) {
        long firstPlayed = player.getFirstPlayed();
        String joined = firstPlayed == 0 ? "unknown" : FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(firstPlayed), ZoneId.systemDefault()));

        String rank;
        if (player.hasPermission("civchat.admin")) {
            rank = "Admin";
        } else if (player.hasPermission("civchat.mod")) {
            rank = "Mod";
        } else if (player.hasPermission("civchat.superfriend")) {
            rank = "Superfriend";
        } else if (player.hasPermission("civchat.bigdonator")) {
            rank = "Big Donator";
        } else if (player.hasPermission("civchat.donator")) {
            rank = "Donator";
        } else {
            rank = "None";
        }

        return HoverEvent.showText(Component.text("Rank: " + rank + "\nJoined: " + joined));
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
        if (player.hasPermission("civchat.bigdonator")) {
            greenStars = 2;
        } else if (player.hasPermission("civchat.donator")) {
            greenStars = 1;
        }

        long firstPlayed = player.getFirstPlayed();
        int yellowStars = firstPlayed == 0 ? 0 : (int) LocalDateTime.ofInstant(Instant.ofEpochMilli(firstPlayed), ZoneId.systemDefault()).until(LocalDateTime.now(), ChronoUnit.YEARS);

        yellowStars = Math.max(0, yellowStars - greenStars);

        StringBuilder stars = new StringBuilder();
        if (yellowStars > 0) {
            stars.append(ChatColor.YELLOW);
        }
        stars.append(STAR.repeat(yellowStars));
        if (greenStars > 0) {
            stars.append(ChatColor.GREEN);
        }
        stars.append(STAR.repeat(greenStars));

        return stars.toString();
    }
}
