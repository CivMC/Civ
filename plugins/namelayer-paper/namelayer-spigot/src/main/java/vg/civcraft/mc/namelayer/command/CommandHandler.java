package vg.civcraft.mc.namelayer.command;

import co.aikar.commands.BaseCommand;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.commands.AcceptInvite;
import vg.civcraft.mc.namelayer.command.commands.AddBlacklist;
import vg.civcraft.mc.namelayer.command.commands.ChangePlayerName;
import vg.civcraft.mc.namelayer.command.commands.CreateGroup;
import vg.civcraft.mc.namelayer.command.commands.DeleteGroup;
import vg.civcraft.mc.namelayer.command.commands.DisciplineGroup;
import vg.civcraft.mc.namelayer.command.commands.GetDefaultGroup;
import vg.civcraft.mc.namelayer.command.commands.GlobalStats;
import vg.civcraft.mc.namelayer.command.commands.GroupStats;
import vg.civcraft.mc.namelayer.command.commands.InfoDump;
import vg.civcraft.mc.namelayer.command.commands.InvitePlayer;
import vg.civcraft.mc.namelayer.command.commands.JoinGroup;
import vg.civcraft.mc.namelayer.command.commands.LeaveGroup;
import vg.civcraft.mc.namelayer.command.commands.ListCurrentInvites;
import vg.civcraft.mc.namelayer.command.commands.ListGroups;
import vg.civcraft.mc.namelayer.command.commands.ListMembers;
import vg.civcraft.mc.namelayer.command.commands.ListPermissions;
import vg.civcraft.mc.namelayer.command.commands.ListPlayerTypes;
import vg.civcraft.mc.namelayer.command.commands.ModifyPermissions;
import vg.civcraft.mc.namelayer.command.commands.NameLayerGroupGui;
import vg.civcraft.mc.namelayer.command.commands.PromotePlayer;
import vg.civcraft.mc.namelayer.command.commands.RejectInvite;
import vg.civcraft.mc.namelayer.command.commands.RemoveBlacklist;
import vg.civcraft.mc.namelayer.command.commands.RemoveMember;
import vg.civcraft.mc.namelayer.command.commands.RevokeInvite;
import vg.civcraft.mc.namelayer.command.commands.SetDefaultGroup;
import vg.civcraft.mc.namelayer.command.commands.SetPassword;
import vg.civcraft.mc.namelayer.command.commands.ShowBlacklist;
import vg.civcraft.mc.namelayer.command.commands.ToggleAutoAcceptInvites;
import vg.civcraft.mc.namelayer.command.commands.TransferGroup;
import vg.civcraft.mc.namelayer.command.commands.UpdateName;

public class CommandHandler {
	CommandManager commandManager;
	public CommandHandler(NameLayerPlugin plugin) {
		commandManager = new CommandManager(plugin);
		commandManager.init();
	}

	public void registerCommands(){
		addCommands(new AcceptInvite());
		addCommands(new CreateGroup());
		addCommands(new DeleteGroup());
		addCommands(new DisciplineGroup());
		addCommands(new GlobalStats());
		addCommands(new GroupStats());
		addCommands(new InfoDump());
		addCommands(new InvitePlayer());
		addCommands(new JoinGroup());
		addCommands(new ListGroups());
		addCommands(new ListMembers());
		addCommands(new ListPermissions());
		//addCommands(new MergeGroups("MergeGroups")); Disabled as it's currently semi broken
		addCommands(new ModifyPermissions());
		addCommands(new RemoveMember());
		addCommands(new SetPassword());
		addCommands(new TransferGroup());
		addCommands(new LeaveGroup());
		addCommands(new ListPlayerTypes());
		addCommands(new ListCurrentInvites());
		addCommands(new ToggleAutoAcceptInvites());
		addCommands(new PromotePlayer());
		addCommands(new RejectInvite());
		addCommands(new RevokeInvite());
		addCommands(new ChangePlayerName());
		addCommands(new SetDefaultGroup());
		addCommands(new GetDefaultGroup());
		addCommands(new UpdateName());
		addCommands(new AddBlacklist());
		addCommands(new RemoveBlacklist());
		addCommands(new ShowBlacklist());
		addCommands(new NameLayerGroupGui());
	}
	
	public void addCommands(BaseCommand command){
			commandManager.registerCommand(command);
	}
}
