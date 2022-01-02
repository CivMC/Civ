package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.chat.Componentify;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public final class BedLocator extends BasicHack {

	private final BedLocatorCommand locatorCommand;

	public BedLocator(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.locatorCommand = new BedLocatorCommand();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		plugin().getCommands().registerCommand(this.locatorCommand);
	}

	@Override
	public void onDisable() {
		plugin().getCommands().unregisterCommand(this.locatorCommand);
		super.onDisable();
	}

	@CommandPermission("simpleadmin.bedlocator")
	public static class BedLocatorCommand extends BaseCommand {
		@CommandAlias("wheresmybed|locatebed")
		@Description("Tells you where your bed is")
		public void giveWand(final Player sender) {
			final var bedLocation = sender.getBedSpawnLocation();
			if (bedLocation == null) {
				sender.sendMessage(Component.text("You do not a set bed.")
						.color(NamedTextColor.GREEN));
				return;
			}
			sender.sendMessage(Component.text()
					.color(NamedTextColor.GREEN)
					.append(Component.text("Your bed is at "))
					.append(Componentify.blockLocation(bedLocation)));
			if (WorldUtils.doLocationsHaveSameWorld(sender.getLocation(), bedLocation)) {
				sender.sendMessage('['
						+ "name:Bed Location,"
						+ "x:" + bedLocation.getBlockX() + ','
						+ "y:" + bedLocation.getBlockY() + ','
						+ "z:" + bedLocation.getBlockZ()
						+ ']');
			}
		}
	}

}
