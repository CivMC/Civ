package vg.civcraft.mc.namelayer.command.commands;

import org.bukkit.command.CommandSender;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;

import java.util.List;
import java.util.UUID;

/**
 * Created by isaac on 2/6/15.
 */
public class ChangePlayerName  extends PlayerCommand {
    public ChangePlayerName(String name) {
        super(name);
        setIdentifier("nlcpn");
        setDescription("This command is used by ops to change a players name");
        setUsage("/nlcpn <old name> <new name>");
        setArguments(2,2);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("namelayer.admin")) {
            sender.sendMessage("You're not an op. ");
            return false;
        }

        UUID player = NameAPI.getUUID(args[0]);
        if (player == null){
            sender.sendMessage(args[0] + " has never logged in");
            return false;
        }

        String newName = args[1].substring(0,16);
        NameAPI.getAssociationList().changePlayer(newName, player);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
