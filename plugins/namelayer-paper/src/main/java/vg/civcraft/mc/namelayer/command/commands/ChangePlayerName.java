package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;

/**
 * Created by isaac on 2/6/15.
 */

public class ChangePlayerName  extends BaseCommandMiddle {

	@CommandAlias("nlcpn|changeplayername")
	@CommandPermission("namelayer.admin")
    @Syntax("<old name> <new name>")
    @Description("Used by ops to change a players name")
    public void execute(CommandSender sender, String currentName, String changedName) {
        if (!sender.isOp() && !sender.hasPermission("namelayer.admin")) {
            sender.sendMessage("You're not an op. ");
            return;
        }
        UUID player = NameAPI.getUUID(currentName);
        if (player == null){
            sender.sendMessage(currentName + " has never logged in");
            return;
        }

        String newName = changedName.length() >= 16 ? changedName.substring(0, 16) : changedName;
        NameAPI.getAssociationList().changePlayer(newName, player);
        NameAPI.resetCache(player);

        sender.sendMessage(currentName + "'s name has been changed to " + newName + ". Have them relog for it to take affect");
    }
}
