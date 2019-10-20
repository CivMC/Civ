package com.github.maxopoly.finale.command;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.NumberConversions;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatConfig;

import net.md_5.bungee.api.ChatColor;
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
			arg0.sendMessage(ChatColor.WHITE + "horizontal: " + ChatColor.RED + cc.getHorizontalKb());
			arg0.sendMessage(ChatColor.WHITE + "vertical: " + ChatColor.RED + cc.getVerticalKb());
			arg0.sendMessage(ChatColor.WHITE + "sprintHorizontal: " + ChatColor.RED + cc.getSprintHorizontal());
			arg0.sendMessage(ChatColor.WHITE + "sprintVertical: " + ChatColor.RED + cc.getSprintVertical());
			arg0.sendMessage(ChatColor.WHITE + "airHorizontal: " + ChatColor.RED + cc.getAirHorizontal());
			arg0.sendMessage(ChatColor.WHITE + "airVertical: " + ChatColor.RED + cc.getAirVertical());
			arg0.sendMessage(ChatColor.WHITE + "attackMotion: " + ChatColor.RED + cc.getAttackMotionModifier());
			arg0.sendMessage(ChatColor.WHITE + "stopSprinting: " + ChatColor.RED + cc.isStopSprinting());
			arg0.sendMessage(ChatColor.WHITE + "noDamageTicks: " + ChatColor.RED + Finale.getPlugin().getManager().getInvulnerableTicks().get(DamageCause.ENTITY_ATTACK));
			arg0.sendMessage(ChatColor.WHITE + "potionCutOffDistance: " + ChatColor.RED + cc.getPotionCutOffDistance());
			return true;
		}
		
		String propName = arg1[0];
		String value = arg1[1];
		
		if (propName.equalsIgnoreCase("stopSprinting")) {
			boolean stopSprinting = value.equalsIgnoreCase("true");
			cc.setStopSprinting(stopSprinting);
			arg0.sendMessage(ChatColor.GREEN + "Set stop sprinting to " + stopSprinting);
			return true;
		}
		
		double num = NumberConversions.toDouble(value);
		
		switch(propName) {
			case "horizontal":
				cc.setHorizontalKb(num);
				break;
			case "vertical":
				cc.setVerticalKb(num);
				break;
			case "sprintHorizontal":
				cc.setSprintHorizontal(num);
				break;
			case "sprintVertical":
				cc.setSprintVertical(num);
				break;
			case "airHorizontal":
				cc.setAirHorizontal(num);
				break;
			case "airVertical":
				cc.setAirVertical(num);
				break;
			case "attackMotion":
				cc.setAttackMotionModifier(num);
				break;
			case "noDamageTicks":
				int invulnTicks = (int) num;
				Finale.getPlugin().getManager().getInvulnerableTicks().put(DamageCause.ENTITY_ATTACK, invulnTicks);
				break;
			case "potionCutOffDistance":
				cc.setPotionCutOffDistance(num);
				break;
		}
		
		arg0.sendMessage(ChatColor.GREEN + "Set " + propName + " to " + num);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return new LinkedList<>();
	}

}
