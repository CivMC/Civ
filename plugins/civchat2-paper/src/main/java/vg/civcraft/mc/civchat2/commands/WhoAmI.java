package vg.civcraft.mc.civchat2.commands;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.NameAPI;

@CivCommand(id = "whoami")
public class WhoAmI extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] arguments) {
		final String response = ChatColor.YELLOW + "You are: " + ChatColor.RESET;
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final String name = NameAPI.getCurrentName(player.getUniqueId());
			if (!Strings.isNullOrEmpty(name)) {
				player.sendMessage(response + name);
				return true;
			}
		}
		sender.sendMessage(response + sender.getName());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] arguments) {
		return new ArrayList<>();
	}

}
