package vg.civcraft.mc.civmodcore.players.scoreboard.bottom;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;

@UtilityClass
public final class BottomLineAPI {

	private static Set<BottomLine> lines = new TreeSet<>();
	private static final String SEPARATOR = ChatColor.BOLD + "  " + ChatColor.BLACK + "||  " + ChatColor.RESET;

	public static void init() {
		BukkitRunnable run = new BukkitRunnable() {

			@Override
			public void run() {
				refreshAll();
			}
		};
		run.runTaskTimer(CivModCorePlugin.getInstance(), 15, 15);
	}

	public static BottomLine createBottomLine(String identifier, int priority) {
		BottomLine line = new BottomLine(identifier, priority);
		lines.add(line);
		return line;
	}

	public static void deleteBottomLine(BottomLine line) {
		lines.remove(line);
	}

	public static void refreshIndividually(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (BottomLine line : lines) {
			String entry = line.getCurrentText(uuid);
			if (entry != null) {
				if (sb.length() > 0) {
					sb.append(SEPARATOR);
				}
				sb.append(entry);
			}
		}
		player.sendActionBar(sb.toString());
	}

	private static void refreshAll() {
		Map<UUID, StringBuilder> texts = new TreeMap<>();
		for (BottomLine line : lines) {
			for (Entry<UUID, String> entry : line.getAll().entrySet()) {
				StringBuilder sb = texts.computeIfAbsent(entry.getKey(), u -> new StringBuilder());
				if (entry.getValue() != null) {
					if (sb.length() > 0) {
						sb.append(SEPARATOR);
					}
					sb.append(entry.getValue());
				}
			}
		}
		for (Entry<UUID, StringBuilder> entry : texts.entrySet()) {
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null) {
				player.sendActionBar(entry.getValue().toString());
			}
		}
	}

}
