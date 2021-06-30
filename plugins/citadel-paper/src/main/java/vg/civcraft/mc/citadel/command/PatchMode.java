package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.PatchState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;

@CommandAlias("ctp")
public class PatchMode extends BaseCommand {

	@Syntax("/ctp <group>")
	@Description("Enters patch mode, which allows you to repair reinforcements. Note that repairing reinforcements will also reset their maturation cycle")
	public void execute(CommandSender sender) {
		Player player = (Player) sender;
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
		AbstractPlayerState currentState = Citadel.getInstance().getStateManager().getState(player);
		if (currentState instanceof PatchState) {
			stateManager.setState(player, null);
		} else {
			stateManager.setState(player, new PatchState(player));
		}
	}
}
