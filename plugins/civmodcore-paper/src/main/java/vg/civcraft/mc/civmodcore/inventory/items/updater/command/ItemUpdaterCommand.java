package vg.civcraft.mc.civmodcore.inventory.items.updater.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

@CommandAlias("debugging")
@CommandPermission("cmc.debug")
public final class ItemUpdaterCommand extends BaseCommand {
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
        if (!(CraftItemStack.unwrap(held).save(MinecraftServer.getServer().registryAccess()) instanceof final CompoundTag heldNbt)) {
            sender.sendMessage(Component.text(
                "That item serialised to an unknown format!",
                NamedTextColor.YELLOW
            ));
            return;
        }
        if (heldNbt.isEmpty()) {
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
                        NbtUtils.structureToSnbt(heldNbt),
                        NamedTextColor.WHITE
                    )
                )
        );
    }

    @Subcommand("updateheld")
    public void updateHeldItem(
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

        final var event = new CommandedUpdateItemEvent(held);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.wasItemUpdated()) {
            sender.sendMessage(Component.text(
                "That item was not updated / needed no update.",
                NamedTextColor.GREEN
            ));
            return;
        }

        sender.sendMessage(Component.text(
            "That item was updated!",
            NamedTextColor.GREEN
        ));
    }
}
