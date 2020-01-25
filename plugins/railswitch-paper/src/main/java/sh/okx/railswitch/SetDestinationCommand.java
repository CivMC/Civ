package sh.okx.railswitch;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetDestinationCommand implements CommandExecutor {
    
    private final RailSwitch plugin;
    
    public SetDestinationCommand(RailSwitch plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!(sender instanceof Player)) {
            return false;
        }
        
        Player player = (Player) sender;
    
        // Convert
        List<String> destinations = new ArrayList<>();
        args: for (String argument : arguments) {
            // If the argument is null or empty, it's likely from a double space, just ignore it
            if (Strings.isNullOrEmpty(argument)) {
                continue;
            }
            if (argument.length() > 40 || !CharMatcher.inRange('0', '9').
                    or(CharMatcher.inRange('a', 'z')).
                    or(CharMatcher.inRange('A', 'Z')).
                    or(CharMatcher.anyOf("!\"#$%&'()*+,-./;:<=>?@[]\\^_`{|}~")).
                    matchesAllOf(argument)) {
                player.sendMessage(ChatColor.RED + "Destinations can not be more than 40 characters and may only " +
                        "use alphanumerical characters, and ASCII symbols.");
                return true;
            }
            for (String destination : destinations) {
                if (argument.equalsIgnoreCase(destination)) {
                    continue args;
                }
            }
            destinations.add(argument);
        }
        
        if (destinations.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "Unset rail destination (use /" + label +
                    " <destination> to set your destination).");
            plugin.getDatabase().removePlayerDestination(player);
            return true;
        }
        
        String destination = String.join(", ", destinations);
        
        long start = System.currentTimeMillis();
        plugin.getDatabase().setPlayerDestination(player, destination);
        if (plugin.isTimings()) {
            plugin.getLogger().info("Set destination took " + (System.currentTimeMillis() - start) + "ms");
        }
        player.sendMessage(ChatColor.GREEN + "Set your rail destination to: " + destination);
        return true;
    }
    
}
