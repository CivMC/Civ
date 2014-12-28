package vg.civcraft.mc.namelayer.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.namelayer.command.commands.AcceptInvite;
import vg.civcraft.mc.namelayer.command.commands.CreateGroup;
import vg.civcraft.mc.namelayer.command.commands.DeleteGroup;
import vg.civcraft.mc.namelayer.command.commands.DisciplineGroup;
import vg.civcraft.mc.namelayer.command.commands.GlobalStats;
import vg.civcraft.mc.namelayer.command.commands.GroupStats;
import vg.civcraft.mc.namelayer.command.commands.InvitePlayer;
import vg.civcraft.mc.namelayer.command.commands.JoinGroup;
import vg.civcraft.mc.namelayer.command.commands.ListGroups;
import vg.civcraft.mc.namelayer.command.commands.ListMembers;
import vg.civcraft.mc.namelayer.command.commands.ListPermissions;
import vg.civcraft.mc.namelayer.command.commands.MergeGroups;
import vg.civcraft.mc.namelayer.command.commands.ModifyPermissions;
import vg.civcraft.mc.namelayer.command.commands.RemoveMember;
import vg.civcraft.mc.namelayer.command.commands.RemoveSuperGroup;
import vg.civcraft.mc.namelayer.command.commands.SetPassword;
import vg.civcraft.mc.namelayer.command.commands.TransferGroup;

public class CommandHandler {
	public Map<String, Command> commands = new HashMap<String, Command>();
	
	public void registerCommands(){
		addCommands(new AcceptInvite("AcceptInvite"));
		// Going to remove super group for now. It will have problems.
		//addCommands(new AddSuperGroup("AddSuperGroup"));
		addCommands(new CreateGroup("CreateGroup"));
		addCommands(new DeleteGroup("DeleteGroup"));
		addCommands(new DisciplineGroup("DisiplineGroup"));
		addCommands(new GlobalStats("GlobalStats"));
		addCommands(new GroupStats("GroupStats"));
		addCommands(new InvitePlayer("InvitePlayer"));
		addCommands(new JoinGroup("JoinGroup"));
		addCommands(new ListGroups("ListGroups"));
		addCommands(new ListMembers("ListMembers"));
		addCommands(new ListPermissions("ListPermissions"));
		addCommands(new MergeGroups("MergeGroups"));
		addCommands(new ModifyPermissions("ModifyPermissions"));
		addCommands(new RemoveMember("RemoveMember"));
		addCommands(new RemoveSuperGroup("RemoveSuperGroup"));
		addCommands(new SetPassword("SetPassword"));
		addCommands(new TransferGroup("TransferGroup"));
	}
	
	public void addCommands(Command command){
			commands.put(command.getIdentifier().toLowerCase(), command);
	}
	
	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args){
		if (commands.containsKey(cmd.getName().toLowerCase())){
			Command command = commands.get(cmd.getName().toLowerCase());
			if (args.length < command.getMinArguments() || args.length > command.getMaxArguments()){
				helpPlayer(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		return true;
	}
	
	public void helpPlayer(Command command, CommandSender sender){
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Command: " ).append(command.getName()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Description: " ).append(command.getDescription()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Usage: ").append(command.getUsage()).toString());
	}
}
