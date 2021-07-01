package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.PatchState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;

public class PatchMode extends BaseCommand {

	@CommandAlias("ctp|patchmode|patch")
	@Description("Enters patch mode, which allows you to repair reinforcements. Note that repairing reinforcements will also reset their maturation cycle")
	@CommandCompletion("@CT_Groups")
	public void execute(Player player) {
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
		AbstractPlayerState currentState = Citadel.getInstance().getStateManager().getState(player);
		if (currentState instanceof PatchState) {
			stateManager.setState(player, null);
		} else {
			stateManager.setState(player, new PatchState(player));
		}
	}
}
