package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.InsecureState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;

@CommandAlias("ctin")
public class Insecure extends BaseCommand {

	@Syntax("/ctin")
	@Description("Enters insecure mode. Interacting with containers in insecure mode will switch their insecure flag. Insecure containers can interact with hoppers reinforced on a different group. All containers are secure by default")
	public void execute(CommandSender sender) {
		Player player = (Player) sender;
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
		AbstractPlayerState currentState = Citadel.getInstance().getStateManager().getState(player);
		if (currentState instanceof InsecureState) {
			stateManager.setState(player, null);
		} else {
			stateManager.setState(player, new InsecureState(player));
		}
	}
}
