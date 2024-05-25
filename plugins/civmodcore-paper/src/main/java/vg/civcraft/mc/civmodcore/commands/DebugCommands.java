package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

@CommandAlias("debugging")
@CommandPermission("cmc.debug")
public final class DebugCommands extends BaseCommand {
	@Subcommand("rawheld")
	public void giveNewItem(
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
		final var raw = new CompoundTag();
		CraftItemStack.asNMSCopy(held).save(raw);
		sender.sendMessage(NbtUtils.structureToSnbt(raw));
	}
}
