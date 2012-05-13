package com.untamedears.citadel.command;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.PluginConsumer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 11:00 AM
 */
public abstract class PlayerCommand extends PluginConsumer implements CommandExecutor {

    protected Player player;
    protected PlayerState state;
    protected int requiredArguments;

    public PlayerCommand(Citadel plugin, int requiredArguments) {
        super(plugin);
        this.requiredArguments = requiredArguments;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            player = (Player) sender;
            state = PlayerState.get(player);
        }
        return args.length >= requiredArguments && player != null && onCommand(command, args);
    }
    
    public abstract boolean onCommand(Command command, String[] args);
}
