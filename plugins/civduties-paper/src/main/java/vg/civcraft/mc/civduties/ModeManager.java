package vg.civcraft.mc.civduties;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civduties.configuration.Command;
import vg.civcraft.mc.civduties.configuration.Command.Timing;
import vg.civcraft.mc.civduties.configuration.Tier;
import vg.civcraft.mc.civduties.database.DatabaseManager;
import vg.civcraft.mc.civduties.external.VaultManager;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;

public class ModeManager {

    private DatabaseManager db;
    private VaultManager vaultManager;
    private Logger logger;

    public ModeManager() {
        db = CivDuties.getInstance().getDatabaseManager();
        vaultManager = CivDuties.getInstance().getVaultManager();
        logger = CivDuties.getInstance().getLogger();
    }

    public boolean isInDuty(UUID uuid) {
        if (db.getPlayerData(uuid) != null) {
            return true;
        }
        return false;
    }

    public boolean isInDuty(Player player) {
        return isInDuty(player.getUniqueId());
    }

    public boolean enableDutyMode(Player player, Tier tier) {
        TagValueOutput nmsCompound = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, ((CraftPlayer) player).getHandle().registryAccess());
        CraftPlayer cPlayer = (CraftPlayer) player;
        cPlayer.getHandle().saveWithoutId(nmsCompound);
        String serverName = Bukkit.getServer().getName();
        db.savePlayerData(player.getUniqueId(), nmsCompound.buildResult(), serverName, tier.getName());

        vaultManager.addPermissionsToPlayer(player, tier.getTemporaryPermissions());
        vaultManager.addPlayerToGroups(player, tier.getTemporaryGroups());

        for (Command command : tier.getCommands()) {
            if (command.getTiming() == Timing.ENABLE) {
                command.execute(player);
            }
        }

        player.sendMessage(ChatColor.RED + "You have entered duty mode. Type /duty to leave it");
        logger.log(Level.INFO, "player " + player.getName() + " has entered duty mode");
        return true;
    }

    public boolean disableDutyMode(Player player, Tier tier) {
        if (!isInDuty(player)) {
            return false;
        }
        NbtCompound input = new NbtCompound(db.getPlayerData(player.getUniqueId()).getData());
        // Inform the client the gamemode was changed to fix graphical issues on the
        // client side
        // Teleport the players using the bukkit api to avoid triggering nocheat
        // movement detection
        double[] location = input.getDoubleArray("Pos", true);
        UUID worldUUID = new UUID(input.getLong("WorldUUIDMost", 0L), input.getLong("WorldUUIDLeast", 0L));
        Location targetLocation = new Location(Bukkit.getWorld(worldUUID), location[0], location[1], location[2]);
        player.teleport(targetLocation);
        player.setGameMode(getGameModeByValue(input.getInt("playerGameType", 0)));
        Bukkit.getScheduler().scheduleSyncDelayedTask(CivDuties.getInstance(), () -> {
            player.teleport(targetLocation);
        }, 3L);
        CraftPlayer cPlayer = (CraftPlayer) player;
        cPlayer.getHandle().load(TagValueInput.createGlobal(ProblemReporter.DISCARDING, input.internal()));

        db.removePlayerData(player.getUniqueId());

        for (Command command : tier.getCommands()) {
            if (command.getTiming() == Timing.DISABLE) {
                command.execute(player);
            }
        }

        vaultManager.removePermissionsFromPlayer(player, tier.getTemporaryPermissions());
        vaultManager.removePlayerFromGroups(player, tier.getTemporaryGroups());

        player.sendMessage(ChatColor.RED + "You have left duty mode");
        logger.log(Level.INFO, "player " + player.getName() + " has left duty mode");
        return true;
    }

    private GameMode getGameModeByValue(int num) {
        switch (num) {
            case 0:
                return GameMode.SURVIVAL;
            case 1:
                return GameMode.CREATIVE;
            case 2:
                return GameMode.ADVENTURE;
            case 3:
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }

}
