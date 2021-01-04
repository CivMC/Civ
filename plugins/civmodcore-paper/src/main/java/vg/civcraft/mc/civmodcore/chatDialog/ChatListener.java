package vg.civcraft.mc.civmodcore.chatDialog;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;

@Deprecated
public class ChatListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void tabComplete(TabCompleteEvent e) {
		if (!(e.getSender() instanceof Player)) {
			return;
		}
		Dialog dia = DialogManager.getDialog((Player) e.getSender());
		if (dia != null) {
			String[] split = e.getBuffer().split(" ");
			List <String> complet = dia.onTabComplete(split.length > 0 ? split[split.length - 1] : "", split);
			if (complet == null) {
				complet = Collections.emptyList();
			}
			e.setCompletions(complet);
		}
	}

	@EventHandler
	public void logoff(PlayerQuitEvent e) {
		DialogManager.forceEndDialog(e.getPlayer());
	}

}
