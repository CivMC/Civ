package vg.civcraft.mc.citadel.command;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.util.TextUtil;

@CivCommand(id = "ctdl")
public class ReinforcementsGUI extends StandaloneCommand {

	private DecimalFormat format = new DecimalFormat("##.##");

	@Override
	public boolean execute(CommandSender sender, String[] arg1) {
		List<ReinforcementType> types = new LinkedList<>(
				Citadel.getInstance().getReinforcementTypeManager().getAllTypes());
		// sort ascending by health
		Collections.sort(types, new Comparator<ReinforcementType>() {

			@Override
			public int compare(ReinforcementType o1, ReinforcementType o2) {
				return Double.compare(o1.getHealth(), o2.getHealth());
			}
		});
		List<IClickable> clicks = new LinkedList<IClickable>();
		for (ReinforcementType type : types) {
			ItemStack is = type.getItem().clone();
			ISUtils.setName(is, ChatColor.AQUA + type.getName());
			ISUtils.addLore(is, ChatColor.GREEN + "Health: " + format.format(type.getHealth()));
			ISUtils.addLore(is, ChatColor.GOLD + "Maturation time: "
					+ TextUtil.formatDuration(type.getMaturationTime(), TimeUnit.MILLISECONDS));
			if (type.getAcidTime() > 0) {
				ISUtils.addLore(is, ChatColor.GOLD + "Acid maturation time: "
						+ TextUtil.formatDuration(type.getAcidTime(), TimeUnit.MILLISECONDS));
			} else {
				ISUtils.addLore(is, ChatColor.GOLD + "Can not be used for acid");
			}
			ISUtils.addLore(is,
					ChatColor.WHITE + "Return chance: " + format.format(type.getReturnChance() * 100.0) + " %");
			IClickable click = new DecorationStack(is);
			clicks.add(click);
		}
		MultiPageView pageView = new MultiPageView((Player) sender, clicks,
				org.bukkit.ChatColor.GOLD + "Reinforcements", true);
		pageView.showScreen();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}

}
