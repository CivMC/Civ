package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.model.CitadelSettingManager;

@CommandAlias("cto")
public class Off extends BaseCommand {

	@Syntax("/cto")
	@Description("Leaves all reinforcement modes")
	public void execute(CommandSender sender) {
		Citadel.getInstance().getStateManager().setState((Player) sender, null);
		CitadelSettingManager settings = Citadel.getInstance().getSettingManager();
		UUID uuid = ((Player) sender).getUniqueId();
		if (settings.getInformationMode().getValue(uuid) && settings.shouldCtoDisableCti(uuid)) {
			settings.getInformationMode().setValue(uuid, false);
		}
		if (settings.getBypass().getValue(uuid) && settings.shouldCtoDisableCtb(uuid)) {
			settings.getBypass().setValue(uuid, false);
		}
	}
}
