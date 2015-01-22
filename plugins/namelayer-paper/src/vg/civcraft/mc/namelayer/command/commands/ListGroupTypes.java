package vg.civcraft.mc.namelayer.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.GroupType;

public class ListGroupTypes extends PlayerCommand{

	public ListGroupTypes(String name) {
		super(name);
		setIdentifier("nllgt");
		setDescription("This command is used to list GroupTypes.");
		setUsage("/nllgt");
		setArguments(0,0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("wereyjukqswedfrgyjukl.");
			return true;
		}
		Player p = (Player) sender;
		GroupType.displayGroupTypes(p);
		return true;
	}

}
