package vg.civcraft.mc.civmodcore.players.settings;

import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.chat.dialog.Dialog;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

public class MenuDialog extends Dialog {
	
	private PlayerSetting<?> setting;
	private MenuSection menu;
	private String errorMsg;

	public MenuDialog(Player player, PlayerSetting<?> setting, MenuSection menu, String errorMsg) {
		super(player, setting.getOwningPlugin(), ChatColor.GOLD + "Enter a new value for " + setting.getNiceName());
		this.setting = setting;
		this.menu = menu;
		this.errorMsg = errorMsg;
	}

	@Override
	public void onReply(String[] message) {
		String result = String.join(" ", message);
		if (!setting.isValidValue(result)) {
			player.sendMessage(ChatColor.RED + errorMsg);
			menu.showScreen(player);
			return;
		}
		setting.setValueFromString(player.getUniqueId(), result);
		player.sendMessage(ChatColor.GREEN + setting.getNiceName() + " set to: " + result);
		menu.showScreen(player);
	}

	@Override
	public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
		return Collections.emptyList();
	}

}
