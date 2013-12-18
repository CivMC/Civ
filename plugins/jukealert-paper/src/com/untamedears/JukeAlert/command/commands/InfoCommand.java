package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.findTargetedOwnedSnitch;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.tasks.GetSnitchInfoPlayerTask;

public class InfoCommand extends PlayerCommand {

    public class History {
        public History(int snitchId, int page) {
            this.snitchId = snitchId;
            this.page = page;
        }
        public int snitchId;
        public int page;
    }

    private static Map<String, History> playerPage_ = new TreeMap<String, History>();

    public InfoCommand() {
        super("Info");
        setDescription("Displays information from a Snitch");
        setUsage("/jainfo <page number or 'next'> [censor]");
        setArgumentRange(0, 2);
        setIdentifier("jainfo");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            final String playerNameLc = player.getName().toLowerCase();
            final Snitch snitch = findTargetedOwnedSnitch(player);
            if (snitch == null) {
                player.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
                return true;
            }
            final int snitchId = snitch.getId();
            int offset = 1;
            if (args.length > 0) {
                try {
                    offset = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    if (playerPage_.containsKey(playerNameLc)) {
                        final History hist = playerPage_.get(playerNameLc);
                        if (hist != null && hist.snitchId == snitchId) {
                            offset = hist.page + 1;
                        } else {
                            offset = 1;
                        }
                    } else {
                        offset = 1;
                    }
                }
            }
            if (offset < 1) {
                offset = 1;
            }
            playerPage_.put(playerNameLc, new History(snitchId, offset));
            sendLog(sender, snitch, offset, args.length == 2);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
            return false;
        }
    }

    private void sendLog(CommandSender sender, Snitch snitch, int offset, boolean shouldCensor) {
        Player player = (Player) sender;
        GetSnitchInfoPlayerTask task = new GetSnitchInfoPlayerTask(plugin, snitch.getId(), snitch.getName(), offset, player, shouldCensor);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);

    }
}
