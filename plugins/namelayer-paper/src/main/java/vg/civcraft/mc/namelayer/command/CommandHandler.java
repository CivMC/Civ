package vg.civcraft.mc.namelayer.command;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import java.util.Arrays;
import java.util.stream.Collectors;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.command.commands.*;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import javax.annotation.Nonnull;

public class CommandHandler extends CommandManager{

	public CommandHandler(NameLayerPlugin plugin) {
		super(plugin);
		init();
	}

	@Override
	public void registerCommands(){
		registerCommand(new AcceptInvite());
		registerCommand(new CreateGroup());
		registerCommand(new DeleteGroup());
		registerCommand(new DisciplineGroup());
		registerCommand(new GlobalStats());
		registerCommand(new GroupStats());
		registerCommand(new InfoDump());
		registerCommand(new InvitePlayer());
		registerCommand(new JoinGroup());
		registerCommand(new ListGroups());
		registerCommand(new ListMembers());
		registerCommand(new ListPermissions());
		//addCommands(new MergeGroups("MergeGroups")); Disabled as it's currently semi broken
		registerCommand(new ModifyPermissions());
		registerCommand(new RemoveMember());
		registerCommand(new SetPassword());
		registerCommand(new TransferGroup());
		registerCommand(new LeaveGroup());
		registerCommand(new ListPlayerTypes());
		registerCommand(new ListCurrentInvites());
		registerCommand(new ToggleAutoAcceptInvites());
		registerCommand(new PromotePlayer());
		registerCommand(new RejectInvite());
		registerCommand(new RevokeInvite());
		registerCommand(new ChangePlayerName());
		registerCommand(new SetDefaultGroup());
		registerCommand(new GetDefaultGroup());
		registerCommand(new UpdateName());
		registerCommand(new AddBlacklist());
		registerCommand(new RemoveBlacklist());
		registerCommand(new ShowBlacklist());
		registerCommand(new NameLayerGroupGui());
	}

	@Override
	public void registerCompletions(@Nonnull CommandCompletions<BukkitCommandCompletionContext> completions) {
		super.registerCompletions(completions);
		completions.registerCompletion("NL_Groups", (context) -> GroupTabCompleter.complete(context.getInput(), null, context.getPlayer()));
		completions.registerAsyncCompletion("NL_Ranks", (context) ->
				Arrays.asList(GroupManager.PlayerType.getStringOfTypes().split(" ")));
		completions.registerCompletion("NL_Perms", (context) ->
				PermissionType.getAllPermissions().stream().map(PermissionType::getName).collect(Collectors.toList()));
	}
}
