package com.untamedears.citadel.command;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PluginConsumer;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import com.untamedears.citadel.entity.Reinforcement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.untamedears.citadel.Utility.sendMessage;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 11:21 AM
 */
public class ListMaterials extends PluginConsumer implements CommandExecutor {

    public ListMaterials(Citadel plugin) {
        super(plugin);
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        if (cmd.equals("ctmaterials")) {
            if (ReinforcementMaterial.VALID.isEmpty()) {
                sendMessage(sender, ChatColor.YELLOW, "No reinforcement materials available.");
            } else {
                List<ReinforcementMaterial> materials = new ArrayList<ReinforcementMaterial>(ReinforcementMaterial.VALID.values());
                Collections.sort(materials);
                sendMessage(sender, ChatColor.GREEN, "Reinforcement materials:");
                for (ReinforcementMaterial m : materials)
                    sendMessage(sender, ChatColor.GREEN, "%s has strength %d and requires %d units.", m.getMaterial().name(), m.getStrength(), m.getRequirements());
            }
        } else if (cmd.equals("ctsecurable")) {
            if (Reinforcement.SECURABLE.isEmpty()) {
                sendMessage(sender, ChatColor.YELLOW, "No other blocks are securable.");
            } else {
                sendMessage(sender, ChatColor.GREEN, getTruncatedMaterialMessage("Securable blocks: ", Reinforcement.SECURABLE));
            }
        } else if (cmd.equals("ctnonreinforceable")) {
            if (Reinforcement.NON_REINFORCEABLE.isEmpty()) {
                sendMessage(sender, ChatColor.YELLOW, "No blocks are non-reinforceable.");
            } else {
                sendMessage(sender, ChatColor.GREEN, getTruncatedMaterialMessage("Non-reinforceable blocks: ", Reinforcement.NON_REINFORCEABLE));
            }
        }
        return true;
    }
    
    private String getTruncatedMaterialMessage(String prefix, List<Integer> materials) {
        StringBuilder builder = new StringBuilder();
        for (int materialId : materials) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(Material.getMaterial(materialId).name());
        }
        builder.insert(0, prefix);
        return builder.toString();
    }
}
