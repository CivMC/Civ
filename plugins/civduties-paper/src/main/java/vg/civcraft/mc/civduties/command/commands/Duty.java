package vg.civcraft.mc.civduties.command.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civduties.CivDuties;
import vg.civcraft.mc.civduties.ModeManager;
import vg.civcraft.mc.civduties.configuration.Tier;
import java.util.List;

public class Duty extends BaseCommand {

    private ModeManager modeManager = CivDuties.getInstance().getModeManager();

    @CommandAlias("duty")
    @Syntax("[player]")
    @Description("Allows you to enter duty mode")
    @CommandPermission("civduties.duty")
    public void execute(Player player, @Optional String targetPlayer) {
        Tier tier = null;

        if (!modeManager.isInDuty(player)) {
            if (targetPlayer == null || targetPlayer.isEmpty()) {
                tier = CivDuties.getInstance().getConfigManager().getTier(player);
            } else {
                tier = CivDuties.getInstance().getConfigManager().getTier(targetPlayer);
                if (!player.hasPermission(tier.getPermission())) {
                    tier = null;
                }
            }

            if (tier == null) {
                player.sendMessage("You don't have permission to execute this command.");
                return;
            }

            if (CivDuties.getInstance().isCombatTagPlusEnabled() && tier.isCombattagBlock()
                && ((CombatTagPlus) Bukkit.getPluginManager().getPlugin("CombatTagPlus")).getTagManager()
                .isTagged(player.getUniqueId())) {
                player.sendMessage("You can't enter duty mode while combat tagged");
                return;
            }
            modeManager.enableDutyMode(player, tier);
        } else {
            String tierName = CivDuties.getInstance().getDatabaseManager().getPlayerData(player.getUniqueId())
                .getTierName();
            tier = CivDuties.getInstance().getConfigManager().getTier(tierName);
            modeManager.disableDutyMode(player, tier);
        }
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("No.");
            return null;
        }

        if (args.length < 2) {
            return CivDuties.getInstance().getConfigManager().getTiersNames((Player) sender);
        }

        return null;
    }
}
