package com.github.maxopoly.finale.command;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatConfig;
import java.util.LinkedList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "combatconfig")
public class CombatConfigCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender arg0, String[] arg1) {
		if (!(arg0 instanceof Player)) {
			return true;
		}
		
		CombatConfig cc = Finale.getPlugin().getManager().getCombatConfig();
		
		if (arg1.length == 0) {
			arg0.sendMessage(ChatColor.WHITE + "knockback: " + ChatColor.RED + cc.getKnockbackMultiplier());
			arg0.sendMessage(ChatColor.WHITE + "sprint: " + ChatColor.RED + cc.getSprintMultiplier());
			arg0.sendMessage(ChatColor.WHITE + "water: " + ChatColor.RED + cc.getWaterKnockbackMultiplier());
			arg0.sendMessage(ChatColor.WHITE + "air: " + ChatColor.RED + cc.getAirKnockbackMultiplier());
			arg0.sendMessage(ChatColor.WHITE + "victim: " + ChatColor.RED + cc.getVictimMotion());
			arg0.sendMessage(ChatColor.WHITE + "maxVictim: " + ChatColor.RED + cc.getMaxVictimMotion());
			arg0.sendMessage(ChatColor.WHITE + "attacker: " + ChatColor.RED + cc.getAttackerMotion());
			arg0.sendMessage(ChatColor.WHITE + "sprintReset: " + ChatColor.RED + cc.isSprintResetEnabled());
			arg0.sendMessage(ChatColor.WHITE + "noDamageTicks: " + ChatColor.RED + Finale.getPlugin().getManager().getInvulnerableTicks().get(DamageCause.ENTITY_ATTACK));
			return true;
		}

		if (arg1.length == 1 && arg1[0].equalsIgnoreCase("save")) {
			cc.save();
			arg0.sendMessage(ChatColor.GREEN + "You have saved the combat config.");
			return true;
		}

		if (arg1.length < 2) {
			arg0.sendMessage(ChatColor.RED + "USAGE: /combatconfig <property> <value>");
			return true;
		}
		
		String propName = arg1[0];
		String value = arg1[1];
		
		if (propName.equalsIgnoreCase("sprintReset")) {
			boolean sprintReset = value.equalsIgnoreCase("true");
			cc.setSprintResetEnabled(sprintReset);
			arg0.sendMessage(ChatColor.GREEN + "Set sprintReset to " + sprintReset);
			return true;
		}

		if (propName.equalsIgnoreCase("noDamageTicks")) {
			int invulnTicks = NumberConversions.toInt(value);
			Finale.getPlugin().getManager().getInvulnerableTicks().put(DamageCause.ENTITY_ATTACK, invulnTicks);
			arg0.sendMessage(ChatColor.GREEN + "Set noDamageTicks to " + invulnTicks);
			return true;
		}

		if (arg1.length < 4) {
			arg0.sendMessage(ChatColor.RED + "USAGE: /combatconfig <property> <x> <y> <z>");
			return true;
		}

		double x = NumberConversions.toDouble(arg1[1]);
		double y = NumberConversions.toDouble(arg1[2]);
		double z = NumberConversions.toDouble(arg1[3]);
		Vector vec = new Vector(x, y, z);
		switch(propName) {
			case "knockback":
				cc.setKnockbackMultiplier(vec);
				break;
			case "sprint":
				cc.setSprintMultiplier(vec);
				break;
			case "water":
				cc.setWaterKnockbackMultiplier(vec);
				break;
			case "air":
				cc.setAirKnockbackMultiplier(vec);
				break;
			case "victim":
				cc.setVictimMotion(vec);
				break;
			case "maxVictim":
				cc.setMaxVictimMotion(vec);
				break;
			case "attacker":
				cc.setAttackerMotion(vec);
				break;
		}
		
		arg0.sendMessage(ChatColor.GREEN + "Set " + propName + " to " + vec);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return new LinkedList<>();
	}

}
