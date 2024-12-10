package vg.civcraft.mc.namelayer.permission;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.UUID;
import vg.civcraft.mc.namelayer.NameLayerPlugin;

public class LuckPermsIntegration {
    private static LuckPerms luckPerms;

    public static void initialize() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            NameLayerPlugin.getInstance().getLogger().info("LuckPerms integration initialized successfully");
        } else {
            NameLayerPlugin.getInstance().getLogger().warning("Failed to initialize LuckPerms integration - provider not found");
        }
    }

    public static void createGroupPermission(String groupName) {
        if (luckPerms == null) {
            NameLayerPlugin.getInstance().getLogger().warning("Failed to create permission for group " + groupName + " - LuckPerms not initialized");
            return;
        }
        String permission = "faction." + groupName.toLowerCase();
        NameLayerPlugin.getInstance().getLogger().info("Permission node created and ready for use: " + permission);
        // We don't actually need to create anything in LuckPerms - the permission node will be created
        // automatically when it's first assigned to a user
    }

    public static void removeGroupPermission(String groupName, UUID playerUuid) {
        if (luckPerms == null) {
            NameLayerPlugin.getInstance().getLogger().warning("Failed to remove permission for player " + playerUuid + " - LuckPerms not initialized");
            return;
        }
        String permission = "faction." + groupName.toLowerCase();
        NameLayerPlugin.getInstance().getLogger().info("Removing permission node " + permission + " from player " + playerUuid);

        // Always try to load the user, don't rely on cached version
        try {
            var userFuture = luckPerms.getUserManager().loadUser(playerUuid);
            var user = userFuture.get(); // Wait for completion
            if (user != null) {
                // Create the node to remove
                Node node = Node.builder(permission)
                    .value(true)
                    .build();
                
                // Remove the permission
                user.data().remove(node);
                
                // Save the changes
                luckPerms.getUserManager().saveUser(user);
                
                NameLayerPlugin.getInstance().getLogger().info("Successfully removed permission node " + permission + " from player " + playerUuid);
            } else {
                NameLayerPlugin.getInstance().getLogger().warning("Failed to remove permission - user could not be loaded: " + playerUuid);
            }
        } catch (Exception e) {
            NameLayerPlugin.getInstance().getLogger().warning("Failed to load user for permission removal: " + playerUuid + " - " + e.getMessage());
        }
    }

    public static void addPlayerToGroup(String groupName, UUID playerUuid) {
        if (luckPerms == null) {
            NameLayerPlugin.getInstance().getLogger().warning("Failed to add permission for player " + playerUuid + " - LuckPerms not initialized");
            return;
        }
        String permission = "faction." + groupName.toLowerCase();
        NameLayerPlugin.getInstance().getLogger().info("Adding permission node " + permission + " to player " + playerUuid);

        // Always try to load the user, don't rely on cached version
        try {
            var userFuture = luckPerms.getUserManager().loadUser(playerUuid);
            var user = userFuture.get(); // Wait for completion
            if (user != null) {
                // Create a new node with the permission
                Node node = Node.builder(permission)
                    .value(true)
                    .build();
                
                // Remove any existing version of this permission first
                user.data().remove(node);
                // Add the new permission
                user.data().add(node);
                
                // Save the changes
                luckPerms.getUserManager().saveUser(user);
                
                NameLayerPlugin.getInstance().getLogger().info("Successfully added permission node " + permission + " to player " + playerUuid);
            } else {
                NameLayerPlugin.getInstance().getLogger().warning("Failed to add permission - user could not be loaded: " + playerUuid);
            }
        } catch (Exception e) {
            NameLayerPlugin.getInstance().getLogger().warning("Failed to load user for permission addition: " + playerUuid + " - " + e.getMessage());
        }
    }

    public static void removePlayerFromGroup(String groupName, UUID playerUuid) {
        removeGroupPermission(groupName, playerUuid);
    }
}