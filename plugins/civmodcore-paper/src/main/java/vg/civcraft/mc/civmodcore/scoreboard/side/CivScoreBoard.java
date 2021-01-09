package vg.civcraft.mc.civmodcore.scoreboard.side;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiFunction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public class CivScoreBoard {

	private String scoreName;
	private Map<UUID, String> currentScoreText;
	private BukkitRunnable updater;

	CivScoreBoard(String scoreName) {
		this.scoreName = scoreName;
		this.currentScoreText = new TreeMap<>();
	}

	public String getName() {
		return scoreName;
	}

	public void updatePeriodically(BiFunction<Player, String, String> updateFunction, long delay) {
		if (updater != null) {
			updater.cancel();
		}
		updater = new BukkitRunnable() {

			@Override
			public void run() {
				Iterator<Entry<UUID, String>>  iter = currentScoreText.entrySet().iterator();
				while(iter.hasNext()) {
					Entry <UUID, String> entry = iter.next();
					Player player = Bukkit.getPlayer(entry.getKey());
					if (player != null) {
						String newText = updateFunction.apply(player, entry.getValue());
						if (newText == null) {
							hideForPlayer(player);
							iter.remove();
							continue;
						}
						if (!newText.equals(entry.getValue())) {
							internalUpdate(player, entry.getValue(), newText);
							entry.setValue(newText);
						}
					}
				}
			}
		};
		updater.runTaskTimer(CivModCorePlugin.getInstance(), delay, delay);
	}

	public void set(Player p, String newText) {
		if (newText == null) {
			hide(p);
			return;
		}
		String oldText = get(p);
		internalUpdate(p, oldText, newText);
		currentScoreText.put(p.getUniqueId(), newText);
	}

	private static void internalUpdate(Player p, String oldText, String newText) {
		if (oldText != null) {
			p.getScoreboard().resetScores(oldText);
		} else {
			ScoreBoardAPI.adjustScore(p.getUniqueId(), 1);
		}
		if (newText.length() > 40) {
			newText = newText.substring(0, 40);
		}
		Score score = getObjective(p).getScore(newText);
		score.setScore(0);
	}

	public String get(Player p) {
		return currentScoreText.get(p.getUniqueId());
	}

	public void hide(Player p) {
		hideForPlayer(p);
		currentScoreText.remove(p.getUniqueId());
	}
	
	private void hideForPlayer(Player p) {
		String text = get(p);
		if (text == null) {
			return;
		}
		p.getScoreboard().resetScores(text);
		ScoreBoardAPI.adjustScore(p.getUniqueId(), -1);
	}

	void tearDown() {
		for (Entry<UUID, String> entry : currentScoreText.entrySet()) {
			Player p = Bukkit.getPlayer(entry.getKey());
			if (p == null) {
				continue;
			}
			p.getScoreboard().resetScores(entry.getValue());
		}
		currentScoreText.clear();
	}

	void purge(Player p) {
		currentScoreText.remove(p.getUniqueId());
	}

	private static Objective getObjective(Player p) {
		Scoreboard scb = p.getScoreboard();
		String title = ScoreBoardAPI.getBoardHeader(p.getUniqueId());
		Objective objective = scb.getObjective(title);
		if (objective == null) {
			scb.getObjectives().forEach(Objective::unregister);
			scb.clearSlot(DisplaySlot.SIDEBAR);
			objective = scb.registerNewObjective(title, title, title);
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		return objective;
	}

}
