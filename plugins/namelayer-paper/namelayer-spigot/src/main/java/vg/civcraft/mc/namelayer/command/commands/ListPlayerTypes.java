package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;

@CommandAlias("nllpt")
public class ListPlayerTypes extends BaseCommandMiddle {

	@Description("List PlayerTypes.")
	@Syntax("/nllpt")
	public void execute(CommandSender sender) {
		if (!(sender instanceof Player)){
			sender.sendMessage("wereyjukqswedfrgyjukl.");
			return;
		}
		Player p = (Player) sender;
		PlayerType.displayPlayerTypesnllpt(p);
	}
}
