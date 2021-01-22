package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.model.CitadelSettingManager;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "cto")
public class Off extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Citadel.getInstance().getStateManager().setState((Player) sender, null);
		CitadelSettingManager settings = Citadel.getInstance().getSettingManager();
		UUID uuid = ((Player) sender).getUniqueId();
		if (settings.getInformationMode().getValue(uuid) && settings.shouldCtoDisableCti(uuid)) {
			settings.getInformationMode().setValue(uuid, false);
		}
		if (settings.getBypass().getValue(uuid) && settings.shouldCtoDisableCtb(uuid)) {
			settings.getBypass().setValue(uuid, false);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}

}
