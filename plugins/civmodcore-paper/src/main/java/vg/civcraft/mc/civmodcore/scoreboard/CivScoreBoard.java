package vg.civcraft.mc.civmodcore.scoreboard;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class CivScoreBoard {

	private String scoreName;
	private Map<UUID, String> currentScoreText;
	private static final String title = "  Info  ";

	CivScoreBoard(String scoreName) {
		this.scoreName = scoreName;
		this.currentScoreText = new TreeMap<>();
	}
	
	public String getName() {
		return scoreName;
	}

	public void set(Player p, String text) {
		String oldText = get(p);
		if (oldText != null) {
			p.getScoreboard().resetScores(oldText);
		}
		else {
			ScoreBoardAPI.adjustScore(p.getUniqueId(), 1);
		}
		Score score = getObjective(p).getScore(text);
		score.setScore(0);
		currentScoreText.put(p.getUniqueId(), text);
	}

	public String get(Player p) {
		return currentScoreText.get(p.getUniqueId());
	}

	public void hide(Player p) {
		String text = get(p);
		if (text == null) {
			return;
		}
		p.getScoreboard().resetScores(text);
		currentScoreText.remove(p.getUniqueId());
		ScoreBoardAPI.adjustScore(p.getUniqueId(), -1);
	}
	
	void tearDown() {
		for(Entry<UUID,String> entry : currentScoreText.entrySet()) {
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

	private Objective getObjective(Player p) {
		Scoreboard scb = p.getScoreboard();
		Objective objective = scb.getObjective(title);
		if (objective == null) {
			scb.getObjectives().forEach(o -> o.unregister());
			scb.clearSlot(DisplaySlot.SIDEBAR);
			objective = scb.registerNewObjective(title, "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		return objective;
	}

}
