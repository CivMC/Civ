package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.util.UUID;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.model.CitadelSettingManager;

public class Off extends BaseCommand {

	@CommandAlias("cto")
	@Description("Leaves all reinforcement modes")
	public void execute(Player sender) {
		Citadel.getInstance().getStateManager().setState(sender, null);
		CitadelSettingManager settings = Citadel.getInstance().getSettingManager();
		UUID uuid = sender.getUniqueId();
		if (settings.getInformationMode().getValue(uuid) && settings.shouldCtoDisableCti(uuid)) {
			settings.getInformationMode().setValue(uuid, false);
		}
		if (settings.getBypass().getValue(uuid) && settings.shouldCtoDisableCtb(uuid)) {
			settings.getBypass().setValue(uuid, false);
		}
	}
}
