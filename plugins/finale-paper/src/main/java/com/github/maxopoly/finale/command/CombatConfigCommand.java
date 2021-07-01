package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class CombatConfigCommand extends BaseCommand {

	@CommandAlias("combatconfig")
	@CommandPermission("finale.cmv")
	@Syntax("<property> <value> [velocityX] [velocityY] [velocityZ]")
	@Description("View/modify combat config values.")
	public void execute(Player sender, String property, String valueName, @Optional String velX, @Optional String velY, @Optional String velZ) {
		CombatConfig cc = Finale.getPlugin().getManager().getCombatConfig();
		
		if (property == null && valueName == null) {
			sender.sendMessage(ChatColor.WHITE + "knockback: " + ChatColor.RED + cc.getKnockbackMultiplier());
			sender.sendMessage(ChatColor.WHITE + "sprint: " + ChatColor.RED + cc.getSprintMultiplier());
			sender.sendMessage(ChatColor.WHITE + "water: " + ChatColor.RED + cc.getWaterKnockbackMultiplier());
			sender.sendMessage(ChatColor.WHITE + "air: " + ChatColor.RED + cc.getAirKnockbackMultiplier());
			sender.sendMessage(ChatColor.WHITE + "victim: " + ChatColor.RED + cc.getVictimMotion());
			sender.sendMessage(ChatColor.WHITE + "maxVictim: " + ChatColor.RED + cc.getMaxVictimMotion());
			sender.sendMessage(ChatColor.WHITE + "attacker: " + ChatColor.RED + cc.getAttackerMotion());
			sender.sendMessage(ChatColor.WHITE + "sprintReset: " + ChatColor.RED + cc.isSprintResetEnabled());
			sender.sendMessage(ChatColor.WHITE + "noDamageTicks: " + ChatColor.RED + Finale.getPlugin().getManager().getInvulnerableTicks().get(DamageCause.ENTITY_ATTACK));
			return;
		}

		if (valueName == null && property.equalsIgnoreCase("save")) {
			cc.save();
			sender.sendMessage(ChatColor.GREEN + "You have saved the combat config.");
			return;
		}
		
		String propName = property;
		String value = valueName;
		
		if (propName.equalsIgnoreCase("sprintReset")) {
			boolean sprintReset = value.equalsIgnoreCase("true");
			cc.setSprintResetEnabled(sprintReset);
			sender.sendMessage(ChatColor.GREEN + "Set sprintReset to " + sprintReset);
			return;
		}

		if (propName.equalsIgnoreCase("noDamageTicks")) {
			int invulnTicks = NumberConversions.toInt(value);
			Finale.getPlugin().getManager().getInvulnerableTicks().put(DamageCause.ENTITY_ATTACK, invulnTicks);
			sender.sendMessage(ChatColor.GREEN + "Set noDamageTicks to " + invulnTicks);
			return;
		}

		double x = NumberConversions.toDouble(velX);
		double y = NumberConversions.toDouble(velY);
		double z = NumberConversions.toDouble(velZ);
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
		sender.sendMessage(ChatColor.GREEN + "Set " + propName + " to " + vec);
	}
}
