package vg.civcraft.mc.namelayer.command;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
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
import vg.civcraft.mc.namelayer.command.commands.ListInvites;
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
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class NameLayerCommands extends CommandManager {
    public NameLayerCommands(
        final @NotNull NameLayerPlugin plugin
    ) {
        super(plugin);
        init();
    }

    @Override
    public void registerCommands() {
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
        registerCommand(new ListInvites());
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
    public void registerCompletions(
        final @NotNull CommandCompletions<BukkitCommandCompletionContext> completions
    ) {
        super.registerCompletions(completions);
        registerGroupCompletion("NL_Groups", completions);

        completions.registerStaticCompletion("NL_Ranks", () -> {
            return Stream.of(GroupManager.PlayerType.values())
                .map(GroupManager.PlayerType::name)
                .toList();
        });
        completions.registerCompletion("NL_Perms", (context) -> {
            return PermissionType.getAllPermissions()
                .stream()
                .map(PermissionType::getName)
                .toList();
        });
        completions.registerCompletion("NL_Invites", (context) -> {
            return PlayerListener.getNotifications(context.getPlayer().getUniqueId())
                .stream()
                .map(Group::getName)
                .toList();
        });
    }

    public static void registerGroupCompletion(
        final @NotNull String identifier,
        final @NotNull CommandCompletions<BukkitCommandCompletionContext> completions
    ) {
        completions.registerCompletion(identifier, (context) -> {
            return NameAPI.getGroupManager().getAllGroupNames(context.getPlayer().getUniqueId());
        });
    }
}
