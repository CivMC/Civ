package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class ReinforcementsGUI extends BaseCommand {

	private DecimalFormat format = new DecimalFormat("##.##");

	@CommandAlias("ctdl|reinforcements")
	@Description("Opens a GUI displaying all reinforcement materials")
	public void execute(Player sender) {
		List<ReinforcementType> types = new LinkedList<>(
				Citadel.getInstance().getReinforcementTypeManager().getAllTypes());
		// sort ascending by health
		Collections.sort(types, (o1, o2) -> Double.compare(o1.getHealth(), o2.getHealth()));
		List<IClickable> clicks = new LinkedList<>();
		for (ReinforcementType type : types) {
			ItemStack is = type.getItem().clone();
			ItemUtils.setDisplayName(is, ChatColor.AQUA + type.getName());
			ItemUtils.addLore(is, ChatColor.GREEN + "Health: " + format.format(type.getHealth()));
			ItemUtils.addLore(is, ChatColor.GOLD + "Maturation time: "
					+ TextUtil.formatDuration(type.getMaturationTime(), TimeUnit.MILLISECONDS));
			if (type.getAcidTime() > 0) {
				ItemUtils.addLore(is, ChatColor.GOLD + "Acid maturation time: "
						+ TextUtil.formatDuration(type.getAcidTime(), TimeUnit.MILLISECONDS));
			} else {
				ItemUtils.addLore(is, ChatColor.GOLD + "Can not be used for acid");
			}
			ItemUtils.addLore(is,
					ChatColor.WHITE + "Return chance: " + format.format(type.getReturnChance() * 100.0) + " %");
			IClickable click = new DecorationStack(is);
			clicks.add(click);
		}
		MultiPageView pageView = new MultiPageView(sender, clicks, ChatColor.BLUE + "Reinforcements", true);
		pageView.showScreen();
	}
}
