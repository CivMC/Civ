package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.reinforcementBroken;
import static com.untamedears.citadel.Utility.timeUntilMature;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.AcidBlockDestroyEvent;

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
        boolean successfulAcid = false;
        Player player = (Player)sender;
        Iterator<Block> itr = new BlockIterator(player, 40); // Within 2.5 chunks
        while (itr.hasNext()) {
            final Block block = itr.next();
            final int mat = block.getTypeId();
            if (mat != acidBlockType) {
                continue;
            }
            IReinforcement rein = Citadel.getReinforcementManager().getReinforcement(block);
            if (!(rein instanceof PlayerReinforcement)) {
                continue;
            }
            if (timeUntilMature(rein) > 0) {
                // Immature acid block
                sender.sendMessage("The acid block isn't ready");
                return true;
            }
            PlayerReinforcement pr = (PlayerReinforcement)rein;
            if (!pr.isAccessible(player)) {
                sender.sendMessage("You cannot use this acid block");
                return true;
            }
            if (pr.getMaxDurability() <= Citadel.getConfigManager().getMaturationInterval()) {
                sender.sendMessage("The acid block isn't strong enough to break anything");
                return true;
            }
            Block above = block.getRelative(BlockFace.UP);
            IReinforcement aboveRein = AccessDelegate.getDelegate(above).getReinforcement();
            if (!(aboveRein instanceof PlayerReinforcement)) {
                // This isn't really an acid block as there's no reinforcement above, ignore
                continue;
            }
            PlayerReinforcement abovePr = (PlayerReinforcement)aboveRein;
            if (abovePr.getMaxDurability() > pr.getMaxDurability()) {
                sender.sendMessage("The acid block isn't strong enough");
                return true;
            }
            // if they try break bedrock, return
            if (above.getType() == Material.BEDROCK
                    || above.getType() == Material.ENDER_PORTAL
                    || above.getType() == Material.ENDER_PORTAL_FRAME) {
                sender.sendMessage("You cant break this block");
                return true;
            }
            // Break block above
            aboveRein.setDurability(0);
            reinforcementBroken(aboveRein);
            above.setType(Material.AIR);
            // Damage acid block
            Double acidDamagePercentage = 
                Citadel.getConfigManager().getAcidBlockReinforcementTax();
            if (acidDamagePercentage > 0.9999999D) {
                pr.setDurability(0);
            } else if (acidDamagePercentage > 0.0000001) {
                int damage = (int)(
                    (double)pr.getScaledMaxDurability() * acidDamagePercentage);
                int durability = pr.getDurability();
                if (durability <= damage) {
                    pr.setDurability(0);
                } else {
                    pr.setDurability(durability - damage);
                }
            }
            // Break acid block
            AcidBlockDestroyEvent event = new AcidBlockDestroyEvent(block);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()){
            	sender.sendMessage(event.getReasonForCancel());
            	return true;
            }
            reinforcementBroken(pr);
            block.breakNaturally();
            successfulAcid = true;
        }
        if (!successfulAcid) {
            sender.sendMessage("No acid block was found");
        }
        return true;
    }
}
