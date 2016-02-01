package vg.civcraft.mc.namelayer.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.group.GroupType;

import java.util.List;

public class ListGroupTypes extends PlayerCommandMiddle{

	public ListGroupTypes(String name) {
		super(name);
		setIdentifier("nllgt");
		setDescription("List GroupTypes.");
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

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
