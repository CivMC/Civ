package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatConfig;
import com.github.maxopoly.finale.misc.knockback.KnockbackConfig;
import com.github.maxopoly.finale.misc.knockback.KnockbackModifier;
import com.github.maxopoly.finale.misc.knockback.KnockbackType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

@CommandAlias("combatconfig")
@CommandPermission("finale.cmv")
public class CombatConfigCommand extends BaseCommand {

    @Default
    @Subcommand("view")
    @Description("View combat config values.")
    public void view(CommandSender sender) {
        CombatConfig cc = Finale.getPlugin().getManager().getCombatConfig();
        sender.sendMessage(ChatColor.WHITE + "normal: ");
        sender.sendMessage(ChatColor.WHITE + "• ground: " + ChatColor.RED + cc.getNormalConfig().getGroundModifier());
        sender.sendMessage(ChatColor.WHITE + "• air: " + ChatColor.RED + cc.getNormalConfig().getAirModifier());
        sender.sendMessage(ChatColor.WHITE + "• water: " + ChatColor.RED + cc.getNormalConfig().getWaterModifier());
        sender.sendMessage(ChatColor.WHITE + "sprint: ");
        sender.sendMessage(ChatColor.WHITE + "• ground: " + ChatColor.RED + cc.getSprintConfig().getGroundModifier());
        sender.sendMessage(ChatColor.WHITE + "• air: " + ChatColor.RED + cc.getSprintConfig().getAirModifier());
        sender.sendMessage(ChatColor.WHITE + "• water: " + ChatColor.RED + cc.getSprintConfig().getWaterModifier());
        sender.sendMessage(ChatColor.WHITE + "victim: " + ChatColor.RED + cc.getVictimMotion());
        sender.sendMessage(ChatColor.WHITE + "maxVictim: " + ChatColor.RED + cc.getMaxVictimMotion());
        sender.sendMessage(ChatColor.WHITE + "attacker: " + ChatColor.RED + cc.getAttackerMotion());
        sender.sendMessage(ChatColor.WHITE + "sprintReset: " + ChatColor.RED + cc.isSprintResetEnabled());
        sender.sendMessage(ChatColor.WHITE + "noDamageTicks: " + ChatColor.RED + Finale.getPlugin().getManager().getInvulnerableTicks().get(DamageCause.ENTITY_ATTACK));
    }

    @Subcommand("save")
    @Description("Save combat config values.")
    public void save(CommandSender sender) {
        CombatConfig cc = Finale.getPlugin().getManager().getCombatConfig();
        cc.save();
        sender.sendMessage(ChatColor.GREEN + "You have saved the combat config.");
    }

    @Subcommand("set")
    @Syntax("<property> <value>|[velX] [velY] [velZ]")
    @Description("Set regular combat config values.")
    public void setNormal(CommandSender sender, String property, String value, @Optional double velY, @Optional double velZ) {
        CombatConfig cc = Finale.getPlugin().getManager().getCombatConfig();

        if (property.equalsIgnoreCase("sprintReset")) {
            boolean sprintReset = value.equalsIgnoreCase("true");
            cc.setSprintResetEnabled(sprintReset);
            sender.sendMessage(ChatColor.GREEN + "Set sprintReset to " + sprintReset);
            return;
        }

        if (property.equalsIgnoreCase("noDamageTicks")) {
            int invulnTicks = NumberConversions.toInt(value);
            Finale.getPlugin().getManager().getInvulnerableTicks().put(DamageCause.ENTITY_ATTACK, invulnTicks);
            sender.sendMessage(ChatColor.GREEN + "Set noDamageTicks to " + invulnTicks);
            return;
        }

        double x = NumberConversions.toDouble(value);
        double y = NumberConversions.toDouble(velY);
        double z = NumberConversions.toDouble(velZ);
        Vector vec = new Vector(x, y, z);
        switch (property) {
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
        sender.sendMessage(ChatColor.GREEN + "Set " + property + " to " + vec);
    }

    @Subcommand("modifier")
    @Syntax("<config> <property> <type> <velX> <velY> <velZ>")
    @Description("Modify knockback modifier combat config values.")
    public void setModifier(CommandSender sender, String config, String property, KnockbackType modifierType, @Optional double velX, @Optional double velY, @Optional double velZ) {
        CombatConfig cc = Finale.getPlugin().getManager().getCombatConfig();

        config = config.toLowerCase();
        property = property.toLowerCase();

        KnockbackConfig knockbackConfig;
        switch (config) {
            case "normal":
                knockbackConfig = cc.getNormalConfig();
                break;
            case "sprint":
                knockbackConfig = cc.getSprintConfig();
                break;
            default:
                knockbackConfig = null;
                break;
        }

        KnockbackModifier modifier = new KnockbackModifier(modifierType, new Vector(velX, velY, velZ));
        switch (property) {
            case "ground":
                knockbackConfig.setGroundModifier(modifier);
                break;
            case "water":
                knockbackConfig.setWaterModifier(modifier);
                break;
            case "air":
                knockbackConfig.setAirModifier(modifier);
                break;
        }
        sender.sendMessage(ChatColor.GREEN + "Set " + config + " " + property + " to " + modifier);
    }
}
