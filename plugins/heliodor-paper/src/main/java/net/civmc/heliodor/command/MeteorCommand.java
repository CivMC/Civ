package net.civmc.heliodor.command;

import net.civmc.heliodor.vein.VeinSpawner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class MeteorCommand implements CommandExecutor {

    private final VeinSpawner spawner;
    private final boolean enabled;

    public MeteorCommand(final VeinSpawner spawner, final boolean enabled) {
        this.spawner = spawner;
        this.enabled = enabled;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                             final @NotNull String label, final @NotNull String[] args) {
        if (!enabled) {
            sender.sendMessage(Component.text("This command is only available on Zorweth.", NamedTextColor.RED));
            return true;
        }
        if (spawner == null) {
            sender.sendMessage(Component.text("Meteoric iron spawning is disabled.", NamedTextColor.RED));
            return true;
        }
        final VeinSpawner.MeteorStatus status = spawner.getAnnouncedMeteorStatus();
        if (!status.active()) {
            sender.sendMessage(Component.text("No active meteor signal remains.", NamedTextColor.GRAY));
            return true;
        }
        sender.sendMessage(Component.text("There was a flash in the sky around " + status.x() + " "
            + status.y() + " " + status.z() + "...", NamedTextColor.GOLD));
        return true;
    }
}
