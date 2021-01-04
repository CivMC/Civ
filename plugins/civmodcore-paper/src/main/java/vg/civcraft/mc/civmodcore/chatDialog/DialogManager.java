package vg.civcraft.mc.civmodcore.chatDialog;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * @deprecated Use {@link vg.civcraft.mc.civmodcore.chat.dialog.DialogManager} instead.
 */
@Deprecated
public class DialogManager {

	private static Map<UUID, Dialog> dialogs = new TreeMap<>();
	
	private DialogManager() {}

	public static Dialog getDialog(Player p) {
		return getDialog(p.getUniqueId());
	}

	public static Dialog getDialog(UUID uuid) {
		return dialogs.get(uuid);
	}

	public static void registerDialog(Player p, Dialog dialog) {
		Dialog current = dialogs.get(p.getUniqueId());
		if (current != null) {
			current.end();
		}
		dialogs.put(p.getUniqueId(), dialog);
	}

	public static void forceEndDialog(Player p) {
		forceEndDialog(p.getUniqueId());
	}

	public static void forceEndDialog(UUID uuid) {
		Dialog dia = dialogs.remove(uuid);
		if (dia != null) {
			dia.end();
		}
	}

}
