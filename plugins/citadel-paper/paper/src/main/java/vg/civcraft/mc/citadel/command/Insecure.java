package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.InsecureState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;

public class Insecure extends BaseCommand {

	@CommandAlias("ctin|insecure")
	@Description("Enters insecure mode. Interacting with containers in insecure mode will switch their insecure flag. Insecure containers can interact with hoppers reinforced on a different group. All containers are secure by default")
	public void execute(Player player) {
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
		AbstractPlayerState currentState = Citadel.getInstance().getStateManager().getState(player);
		if (currentState instanceof InsecureState) {
			stateManager.setState(player, null);
		} else {
			stateManager.setState(player, new InsecureState(player));
		}
	}
}
