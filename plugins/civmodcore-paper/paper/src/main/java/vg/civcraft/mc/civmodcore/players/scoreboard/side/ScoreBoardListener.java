package vg.civcraft.mc.civmodcore.players.scoreboard.side;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ScoreBoardListener implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void join(PlayerJoinEvent e) {
		e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

}
