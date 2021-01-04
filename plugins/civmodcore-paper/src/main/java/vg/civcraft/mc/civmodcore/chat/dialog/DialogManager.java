package vg.civcraft.mc.civmodcore.chat.dialog;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;

public class DialogManager implements Listener {

	public static final DialogManager INSTANCE = new DialogManager();
	private static final Map<UUID, Dialog> DIALOGS = new TreeMap<>();
	
	private DialogManager() {}

	public static Dialog getDialog(final UUID player) {
		return DIALOGS.get(player);
	}

	public static void registerDialog(final UUID player, final Dialog dialog) {
		Preconditions.checkNotNull(player, "Player cannot be null!");
		Preconditions.checkNotNull(dialog, "Dialog cannot be null!");
		forceEndDialog(player); // Unregister any existing dialog
		DIALOGS.put(player, dialog);
	}

	public static void forceEndDialog(final UUID player) {
		final Dialog dialog = DIALOGS.remove(player);
		if (dialog != null) {
			dialog.end();
		}
	}

	public static void resetDialogs() {
		DIALOGS.forEach((player, dialog) -> dialog.end());
		DIALOGS.clear();
	}

	// LISTENER PART

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void tabComplete(final TabCompleteEvent event) {
		final CommandSender sender = event.getSender();
		if (!(sender instanceof Player)) {
			return;
		}
		final Player player = (Player) sender;
		final Dialog dialog = getDialog(player.getUniqueId());
		if (dialog == null) {
			return;
		}
		final String[] arguments = event.getBuffer().split(" ");
		final String lastArgument = arguments.length == 0 ? "" : arguments[arguments.length - 1];
		List<String> completed = dialog.onTabComplete(lastArgument, arguments);
		if (completed == null) {
			completed = new ArrayList<>();
		}
		event.setCompletions(completed);
	}

	@EventHandler
	public void playerExit(final PlayerQuitEvent event) {
		forceEndDialog(event.getPlayer().getUniqueId());
	}

}
