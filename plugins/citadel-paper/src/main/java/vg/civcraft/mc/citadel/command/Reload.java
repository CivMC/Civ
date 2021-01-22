package vg.civcraft.mc.citadel.command;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "citadelreload")
public class Reload extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Citadel.getInstance().reload();
		sender.sendMessage(ChatColor.GREEN + "Reloaded Citadel");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<>();
	}

}
