package vg.civcraft.mc.civmodcore.chat;

import javax.annotation.Nonnull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public final class Componentify {

	private static Component INTERNAL_addLocationWorld(final Location location) {
		if (location.isWorldLoaded()) {
			return Component.text(location.getWorld().getName())
					.hoverEvent(HoverEvent.showText(Component.text("World name")));
		}
		else {
			return Component.text("<null>")
					.color(NamedTextColor.RED)
					.hoverEvent(HoverEvent.showText(Component.text("World not specified / loaded")));
		}
	}

	public static Component fullLocation(@Nonnull final Location location) {
		final var component = Component.text();
		component.append(INTERNAL_addLocationWorld(location));
		component.append(Component.space());
		component.append(Component.text(location.getX())
				.color(NamedTextColor.RED)
				.hoverEvent(HoverEvent.showText(Component.text("X"))));
		component.append(Component.space());
		component.append(Component.text(location.getY())
				.color(NamedTextColor.GREEN)
				.hoverEvent(HoverEvent.showText(Component.text("Y"))));
		component.append(Component.space());
		component.append(Component.text(location.getZ())
				.color(NamedTextColor.BLUE)
				.hoverEvent(HoverEvent.showText(Component.text("Z"))));
		component.append(Component.space());
		component.append(Component.text(location.getYaw())
				.color(NamedTextColor.GOLD)
				.hoverEvent(HoverEvent.showText(Component.text("Yaw"))));
		component.append(Component.space());
		component.append(Component.text(location.getPitch())
				.color(NamedTextColor.AQUA)
				.hoverEvent(HoverEvent.showText(Component.text("Pitch"))));
		return component.build();
	}

	public static Component blockLocation(@Nonnull final Location location) {
		final var component = Component.text();
		component.append(INTERNAL_addLocationWorld(location));
		component.append(Component.space());
		component.append(Component.text(location.getBlockX())
				.color(NamedTextColor.RED)
				.hoverEvent(HoverEvent.showText(Component.text("Block X"))));
		component.append(Component.space());
		component.append(Component.text(location.getBlockY())
				.color(NamedTextColor.GREEN)
				.hoverEvent(HoverEvent.showText(Component.text("Block X"))));
		component.append(Component.space());
		component.append(Component.text(location.getBlockZ())
				.color(NamedTextColor.BLUE)
				.hoverEvent(HoverEvent.showText(Component.text("Block X"))));
		return component.build();
	}

}
