package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.findLookingAtOrClosestSnitch;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;


public class NameCommand extends PlayerCommand {

    public NameCommand() {
        super("Name");
        setDescription("Set snitch name");
        setUsage("/janame <name>");
        setArguments(1, 1);
        setIdentifier("janame");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            String name = "";
            if (args[0].length() > 40) {
                name = args[0].substring(0, 40);
            } else {
            	name = args[0];
            }
            Snitch snitch = findLookingAtOrClosestSnitch(player, PermissionType.getPermission("RENAME_SNITCH"));
            if (snitch != null) {
            	JukeAlert plugin = JukeAlert.getInstance();
            	plugin.getJaLogger().updateSnitchName(snitch, name);
            	snitch.setName(name);
            	sender.sendMessage(ChatColor.AQUA + " Changed snitch name to " + name);
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You do not own any snitches nearby or lack permission to rename them!");
            return false;
        }
    }

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
