package vg.civcraft.mc.civmodcore.chatDialog;

import java.util.Collection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void tabComplete(PlayerChatTabCompleteEvent e) {
		Dialog dia = DialogManager.instance.getDialog(e.getPlayer());
		if (dia != null) {
			Collection<String> completes = e.getTabCompletions();
			completes.clear();
			completes.addAll(dia.onTabComplete(e.getLastToken(), e.getChatMessage().split(" ")));
		}
	}

	@EventHandler
	public void logoff(PlayerQuitEvent e) {
		DialogManager.instance.forceEndDialog(e.getPlayer());
	}

}
