package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdAdminSetCaptureLoc extends PearlCommand {
    public CmdAdminSetCaptureLoc(
        final @NotNull ExilePearlApi pearlApi
    ) {
        super(pearlApi);

        this.aliases.add("setcaptureloc");

        this.setHelpShort("Sets the capture-location of the pearl");

        this.commandArgs.add(requiredPearlPlayer());
        this.commandArgs.add(optional("world"));
        this.commandArgs.add(optional("x"));
        this.commandArgs.add(optional("y"));
        this.commandArgs.add(optional("z"));

        this.permission = Permission.SET_CAPTURE_LOC.node;
        this.visibility = CommandVisibility.SECRET;
    }

    @Override
    public void perform() {
        final UUID victimUuid = argAsPlayerOrUUID(0);
        if (victimUuid == null) {
            msg("<i>No player was found matching <c>%s", argAsString(0));
            return;
        }
        final ExilePearl pearl = this.plugin.getPearl(victimUuid);
        if (pearl == null) {
            msg("<i>No pearl was found matching <c>%s", argAsString(0));
            return;
        }

        final Location location;
        if (Bukkit.getPlayer(victimUuid) instanceof final Player onlineVictim) {
            location = onlineVictim.getLocation().toBlockLocation().set(0, 0, 0);
        }
        else if (this.sender instanceof final Player playerSender) {
            location = playerSender.getLocation().toBlockLocation().set(0, 0, 0);
        }
        else {
            location = new Location(null, 0, 0, 0);
        }

        int locationArg = 0; // set to zero so first increment results in 1 (2nd argument)
        switch (this.args.size()) {
            case 0 + 1: {
                pearl.setCaptureLocation(null);
                msg("<g>Capture location for [%s] cleared!".formatted(
                    pearl.getPlayerName()
                ));
                return;
            }
            // [world x y z]
            case 4 + 1: {
                final World world = Bukkit.getWorld(argAsString(++locationArg));
                if (world == null) {
                    msg("<i>Invalid world!");
                    return;
                }
                location.setWorld(world);
            }
            // [x y z]
            case 3 + 1: {
                final Integer x = argAsInt(++locationArg);
                final Integer y = argAsInt(++locationArg);
                final Integer z = argAsInt(++locationArg);
                if (x == null || y == null || z == null) {
                    msg("<i>Invalid coordinates!");
                    return;
                }
                location.set(x, y, z);
                break;
            }
            default: {
                msg("<i>Invalid location!");
                return;
            }
        }

        final World world = location.getWorld();
        if (world == null) {
            msg("<i>Location must have a world!");
            return;
        }

        pearl.setCaptureLocation(location);
        msg("<g>Capture location for [%s] set to [%s %d %d %d]!".formatted(
            pearl.getPlayerName(),
            world.getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        ));
    }
}
