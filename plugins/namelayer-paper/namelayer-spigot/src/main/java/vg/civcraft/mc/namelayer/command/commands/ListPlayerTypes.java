package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;

public class ListPlayerTypes extends PlayerCommandMiddle{

	public ListPlayerTypes(String name) {
		super(name);
		setIdentifier("nllpt");
		setDescription("List PlayerTypes.");
		setUsage("/nllpt");
		setArguments(0,0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("wereyjukqswedfrgyjukl.");
			return true;
		}
		Player p = (Player) sender;
		PlayerType.displayPlayerTypesnllpt(p);
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
