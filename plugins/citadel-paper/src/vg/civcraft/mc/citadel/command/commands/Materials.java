package vg.civcraft.mc.citadel.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.command.PlayerCommand;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;

public class Materials extends PlayerCommand{

	public Materials(String name) {
		super(name);
		setIdentifier("ctm");
		setDescription("Shows a list of all the ReinforcementTypes.");
		setUsage("/ctm");
		setArguments(0,0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("Must be a player to run this command.");
			return true;
		}
		Player p = (Player) sender;
		List<ReinforcementType> types = ReinforcementType.getReinforcementTypes();
		String t = "The ItemStacks are as follows: \n";
		for (ReinforcementType type: types){
			t += type.getMaterial().name() + ":\n   ";
			t += "Amount: " + type.getRequiredAmount() + ".\n   ";
			t += "Durability: " + type.getHitPoints() + ".\n   ";
			t += "Material: " + type.getMaterial() + ".\n   ";
			if (type.getItemStack().getItemMeta().hasLore()){
				t += "Lore: ";
				for (String x: type.getItemStack().getItemMeta().getLore())
					t += x + "\n         ";
			}
			t += "-------\n";
		}
		p.sendMessage(ChatColor.GREEN + t);
		return true;
	}

}
