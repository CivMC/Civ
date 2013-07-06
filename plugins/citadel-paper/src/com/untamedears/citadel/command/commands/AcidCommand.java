package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.reinforcementBroken;
import static com.untamedears.citadel.Utility.timeUntilMature;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class AcidCommand extends PlayerCommand {

    public AcidCommand() {
        super("Acid block");
        setDescription("Trigger acid block to eat a reinforcement");
        setUsage("/ctacid");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] {"ctacid"});
    }

    public boolean execute(CommandSender sender, String[] args) {
        Integer acidBlockType = Citadel.getConfigManager().getAcidBlockType();
        if (acidBlockType == null) {
            sender.sendMessage("Acid blocks are disabled");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only command");
            return true;
        }
        boolean immature = false;
        Player player = (Player)sender;
        String playerName = player.getName();
        List<Block> lastTwo = player.getLastTwoTargetBlocks(null, 64);
        for (Block block : lastTwo) {
            int mat = block.getTypeId();
            if (mat != acidBlockType) {
                continue;
            }
            IReinforcement rein = Citadel.getReinforcementManager().getReinforcement(block);
            if (!(rein instanceof PlayerReinforcement)) {
                continue;
            }
            if (timeUntilMature(rein) > 0) {
                // Immature acid blocks
                immature = true;
                continue;
            }
            PlayerReinforcement pr = (PlayerReinforcement)rein;
            if (!pr.isAccessible(playerName)) {
                continue;
            }
            Block above = block.getRelative(BlockFace.UP);
            IReinforcement aboveRein = Citadel.getReinforcementManager().getReinforcement(above);
            if (!(aboveRein instanceof PlayerReinforcement)) {
                continue;
            }
            PlayerReinforcement abovePr = (PlayerReinforcement)aboveRein;
            if (abovePr.getMaxDurability() > pr.getMaxDurability()) {
                reinforcementBroken(pr);
                block.breakNaturally();
                sender.sendMessage("The acid block isn't strong enough");
                return true;
            }
            // Break block above
            aboveRein.setDurability(0);
            reinforcementBroken(aboveRein);
            above.setType(Material.AIR);
            // Break acid block
            reinforcementBroken(pr);
            block.breakNaturally();
            return true;
        }
        if (immature) {
            sender.sendMessage("The acid block isn't ready");
        } else {
            sender.sendMessage("No acid block was found");
        }
        return true;
    }
}
