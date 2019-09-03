package vg.civcraft.mc.civmodcore.playersettings;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.chatDialog.Dialog;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

public class MenuDialog extends Dialog {
	
	private StringInputSetting<?> setting;
	private MenuSection menu;
	private String errorMsg;

	public MenuDialog(Player player, StringInputSetting<?> setting, MenuSection menu, String errorMsg) {
		super(player, setting.getOwningPlugin(), ChatColor.GOLD + "Enter a new value for " + setting.getNiceName());
		this.setting = setting;
		this.menu = menu;
		this.errorMsg = errorMsg;
	}

	@Override
	public void onReply(String[] message) {
		StringBuilder sb = new StringBuilder();
		for(String part : message) {
			sb.append(part);
			sb.append(" ");
		}
		String result;
		if (sb.length() == 0) {
			result = "";
		}
		else {
			result = sb.substring(0, sb.length() - 1);
		}
		if (!setting.isValidValue(result)) {
			player.sendMessage(ChatColor.RED + errorMsg);
			menu.showScreen(player);
			return;
		}
		setting.setValueFromString(player.getUniqueId(), result);
		player.sendMessage(ChatColor.GREEN + "Set " + setting.getNiceName() + " to " + result);
		menu.showScreen(player);
	}

	@Override
	public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
		return new LinkedList<>();
	}

}
