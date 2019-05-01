package vg.civcraft.mc.civmodcore.chatDialog;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;

public class ChatListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void tabComplete(TabCompleteEvent e) {
		if (!(e.getSender() instanceof Player)) {
			e.getSender().sendMessage("You are not human");
			return;
		}
		Dialog dia = DialogManager.instance.getDialog((Player) e.getSender());
		if (dia != null) {
			String[] split = e.getBuffer().split(" ");
			e.setCompletions(dia.onTabComplete(split.length > 0 ? split[split.length - 1] : "", split));
		}
	}

	@EventHandler
	public void logoff(PlayerQuitEvent e) {
		DialogManager.instance.forceEndDialog(e.getPlayer());
	}

}
