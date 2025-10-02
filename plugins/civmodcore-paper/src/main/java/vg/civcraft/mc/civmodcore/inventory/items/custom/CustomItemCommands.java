package vg.civcraft.mc.civmodcore.inventory.items.custom;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import java.util.ArrayList;
import java.util.List;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.DebugCommands;
import vg.civcraft.mc.civmodcore.commands.TabComplete;

@CommandAlias(DebugCommands.DEBUG_COMMAND_ROOT)
@CommandPermission(DebugCommands.DEBUG_COMMAND_PERMISSION)
public final class CustomItemCommands extends BaseCommand {

	@Subcommand("give-custom-item")
    @Syntax("<group> [amount]")
    @Description("Get a custom item by its key.")
    @CommandCompletion("@Custom_Civ_Item")
	public void giveCustomItem(
		final @NotNull Player sender,
		final @NotNull String customKey,
		final @Optional @Default("1") int amount
	) {
		if (amount < 1) {
			throw new InvalidCommandArgument("amount cannot be less than 1!");
		}
		final ItemStack customItem = CustomItem.getCustomItem(customKey);
		if (customItem == null) {
			sender.sendMessage(Component.text(
				"That custom-item key [" + customKey + "] is not registered!",
				NamedTextColor.RED
			));
			return;
		}
        customItem.setAmount(amount);
		sender.give(List.of(customItem), true);
		sender.sendMessage(Component.text(
			"You've been given " + amount + " " + customKey + "!",
			NamedTextColor.GREEN
		));
	}

    @TabComplete("Custom_Civ_Item")
    public List<String> tabComplete(BukkitCommandCompletionContext context) {
        return new ArrayList<>(CustomItem.getRegisteredKeys());
    }
}
