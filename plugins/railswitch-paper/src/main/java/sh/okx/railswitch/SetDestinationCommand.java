package sh.okx.railswitch;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The Set Destination command handler.
 */
public class SetDestinationCommand implements CommandExecutor {
    
    private static final Pattern validDestinationPattern = Pattern.compile(
            "^[\\w!\"#$%&'()*+,-./;:<=>?@\\[\\]^`{|}~]{1,40}$");
    
    private final RailSwitchPlugin plugin;
    
    /**
     * Creates a new set destination command handler, passing through the current RailSwitch plugin instance.
     *
     * @param plugin The plugin to pass through to this command handler.
     */
    public SetDestinationCommand(RailSwitchPlugin plugin) {
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
        checker:
        for (String argument : arguments) {
            // If the argument is null or empty, it's likely from a double space, just ignore it
            if (Strings.isNullOrEmpty(argument)) {
                continue;
            }
            if (!validDestinationPattern.matcher(argument).matches()) {
                player.sendMessage(ChatColor.RED + "Destinations can not be more than 40 characters and may only " +
                        "use alphanumerical characters, and ASCII symbols.");
                return true;
            }
            for (String destination : destinations) {
                if (argument.equalsIgnoreCase(destination)) {
                    continue checker;
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
        
        String destination = String.join(" ", destinations);
        
        if (destination.length() > 40) {
            player.sendMessage(ChatColor.RED + "Destinations as a whole cannot have more than 40 characters.");
            return true;
        }
        
        long start = System.currentTimeMillis();
        plugin.getDatabase().setPlayerDestination(player, destination);
        if (plugin.isDebug()) {
            plugin.getLogger().info("Set destination took " + (System.currentTimeMillis() - start) + "ms");
        }
        player.sendMessage(ChatColor.GREEN + "Set your rail destination to: " + destination);
        return true;
    }
    
}
