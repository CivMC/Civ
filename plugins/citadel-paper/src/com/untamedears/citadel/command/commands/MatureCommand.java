package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.timeUntilMature;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class MatureCommand extends PlayerCommand {

    public MatureCommand() {
        super("Insta-mature");
        setDescription("Instantly mature a reinforcement");
        setUsage("/ctmature");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] {"ctmature"});
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only command");
            return true;
        }
        Player player = (Player)sender;
        if (player.getGameMode() != GameMode.CREATIVE) {
            sender.sendMessage("Access denied");
            return true;
        }
        List<Block> lastTwo = player.getLastTwoTargetBlocks(null, 64);
        for (Block block : lastTwo) {
            IReinforcement rein = Citadel.getReinforcementManager().getReinforcement(block);
            if (!(rein instanceof PlayerReinforcement)) {
                continue;
            }
            if (timeUntilMature(rein) <= 0) {
                continue;
            }
            PlayerReinforcement pr = (PlayerReinforcement)rein;
            pr.setMaturationTime(0);
            Citadel.getReinforcementManager().addReinforcement(pr);
            sender.sendMessage("Reinforcement matured");
            return true;
        }
        sender.sendMessage("No immature reinforcements under cursor");
        return true;
    }
}
