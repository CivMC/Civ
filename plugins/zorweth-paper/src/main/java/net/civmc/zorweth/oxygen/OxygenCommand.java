package net.civmc.zorweth.oxygen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class OxygenCommand implements CommandExecutor {

    private final OxygenManager oxygenManager;

    public OxygenCommand(final OxygenManager oxygenManager) {
        this.oxygenManager = oxygenManager;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                             final @NotNull String label, final @NotNull String[] args) {
        if (!sender.hasPermission("zorweth.admin")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Oxygen: " + this.oxygenManager.getOxygen(player), NamedTextColor.AQUA));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /oxygen [number]", NamedTextColor.RED));
            return true;
        }

        final double oxygen;
        try {
            oxygen = Double.parseDouble(args[0]);
        } catch (final NumberFormatException exception) {
            sender.sendMessage(Component.text("Oxygen must be a number.", NamedTextColor.RED));
            return true;
        }

        if (!Double.isFinite(oxygen)) {
            sender.sendMessage(Component.text("Oxygen must be a finite number.", NamedTextColor.RED));
            return true;
        }

        this.oxygenManager.setOxygen(player, oxygen);
        sender.sendMessage(Component.text("Oxygen set to " + oxygen + ".", NamedTextColor.GREEN));
        return true;
    }
}
