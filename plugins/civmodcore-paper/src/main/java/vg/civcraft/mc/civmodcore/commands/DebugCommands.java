package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

@CommandAlias(DebugCommands.DEBUG_COMMAND_ROOT)
@CommandPermission(DebugCommands.DEBUG_COMMAND_PERMISSION)
public final class DebugCommands extends BaseCommand {
    public static final String DEBUG_COMMAND_ROOT = "debugging";
    public static final String DEBUG_COMMAND_PERMISSION = "cmc.debug";

    @Subcommand("rawheld")
    public void printHeldItemNbt(
        final @NotNull Player sender
    ) {
        final ItemStack held = sender.getInventory().getItemInMainHand();

        if (ItemUtils.isEmptyItem(held)) {
            sender.sendMessage(Component.text(
                "That item is empty!",
                NamedTextColor.YELLOW
            ));
            return;
        }

        // Convert the raw item NBT to SNBT and print
        final JsonObject json = Bukkit.getUnsafe().serializeItemAsJson(held);
        if (json.isEmpty()) {
            sender.sendMessage(Component.text(
                "That item serialised to nothing!",
                NamedTextColor.YELLOW
            ));
            return;
        }

        sender.sendMessage(
            Component.text()
                .color(NamedTextColor.GREEN)
                .append(
                    Component.text("Showing NBT for "),
                    held.displayName(),
                    Component.text(":"),
                    Component.newline(),
                    Component.text(
                        json.toString(),
                        NamedTextColor.WHITE
                    )
                )
        );
    }
}
