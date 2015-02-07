package vg.civcraft.mc.namelayer.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.command.PlayerCommand;

import java.util.List;

public class ListPlayerTypes extends PlayerCommand{

	public ListPlayerTypes(String name) {
		super(name);
		setIdentifier("nllpt");
		setDescription("This command is used to list PlayerTypes.");
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
