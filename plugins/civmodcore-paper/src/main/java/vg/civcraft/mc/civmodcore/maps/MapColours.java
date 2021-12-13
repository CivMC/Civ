package vg.civcraft.mc.civmodcore.maps;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.world.level.material.MaterialColor;
import org.apache.commons.collections4.CollectionUtils;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

/**
 * This is a mapped version of NMS class {@link MaterialColor} to make setting pixel colours easier.
 *
 * <a href="https://minecraft.fandom.com/wiki/Map_item_format#Base_colors">Read more.</a>
 *
 * Deobf path: net.minecraft.world.level.material.MaterialColor
 */
public enum MapColours {

	NONE(MaterialColor.NONE),
	GRASS(MaterialColor.GRASS),
	SAND(MaterialColor.SAND),
	WOOL(MaterialColor.WOOL), // White wool
	FIRE(MaterialColor.FIRE),
	ICE(MaterialColor.ICE),
	METAL(MaterialColor.METAL),
	PLANT(MaterialColor.PLANT),
	SNOW(MaterialColor.SNOW),
	CLAY(MaterialColor.CLAY),
	DIRT(MaterialColor.DIRT),
	STONE(MaterialColor.STONE),
	WATER(MaterialColor.WATER),
	WOOD(MaterialColor.WOOD),
	QUARTZ(MaterialColor.QUARTZ),
	COLOR_ORANGE(MaterialColor.COLOR_ORANGE),
	COLOR_MAGENTA(MaterialColor.COLOR_MAGENTA),
	COLOR_LIGHT_BLUE(MaterialColor.COLOR_LIGHT_BLUE),
	COLOR_YELLOW(MaterialColor.COLOR_YELLOW),
	COLOR_LIGHT_GREEN(MaterialColor.COLOR_LIGHT_GREEN),
	COLOR_PINK(MaterialColor.COLOR_PINK),
	COLOR_GRAY(MaterialColor.COLOR_GRAY),
	COLOR_LIGHT_GRAY(MaterialColor.COLOR_LIGHT_GRAY),
	COLOR_CYAN(MaterialColor.COLOR_CYAN),
	COLOR_PURPLE(MaterialColor.COLOR_PURPLE),
	COLOR_BLUE(MaterialColor.COLOR_BLUE),
	COLOR_BROWN(MaterialColor.COLOR_BROWN),
	COLOR_GREEN(MaterialColor.COLOR_GREEN),
	COLOR_RED(MaterialColor.COLOR_RED),
	COLOR_BLACK(MaterialColor.COLOR_BLACK),
	GOLD(MaterialColor.GOLD),
	DIAMOND(MaterialColor.DIAMOND),
	LAPIS(MaterialColor.LAPIS),
	EMERALD(MaterialColor.EMERALD),
	PODZOL(MaterialColor.PODZOL),
	NETHER(MaterialColor.NETHER),
	TERRACOTTA_WHITE(MaterialColor.TERRACOTTA_WHITE),
	TERRACOTTA_ORANGE(MaterialColor.TERRACOTTA_ORANGE),
	TERRACOTTA_MAGENTA(MaterialColor.TERRACOTTA_MAGENTA),
	TERRACOTTA_LIGHT_BLUE(MaterialColor.TERRACOTTA_LIGHT_BLUE),
	TERRACOTTA_YELLOW(MaterialColor.TERRACOTTA_YELLOW),
	TERRACOTTA_LIGHT_GREEN(MaterialColor.TERRACOTTA_LIGHT_GREEN),
	TERRACOTTA_PINK(MaterialColor.TERRACOTTA_PINK),
	TERRACOTTA_GRAY(MaterialColor.TERRACOTTA_GRAY),
	TERRACOTTA_LIGHT_GRAY(MaterialColor.TERRACOTTA_LIGHT_GRAY),
	TERRACOTTA_CYAN(MaterialColor.TERRACOTTA_CYAN),
	TERRACOTTA_PURPLE(MaterialColor.TERRACOTTA_PURPLE),
	TERRACOTTA_BLUE(MaterialColor.TERRACOTTA_BLUE),
	TERRACOTTA_BROWN(MaterialColor.TERRACOTTA_BROWN),
	TERRACOTTA_GREEN(MaterialColor.TERRACOTTA_GREEN),
	TERRACOTTA_RED(MaterialColor.TERRACOTTA_RED),
	TERRACOTTA_BLACK(MaterialColor.TERRACOTTA_BLACK),
	CRIMSON_NYLIUM(MaterialColor.CRIMSON_NYLIUM),
	CRIMSON_STEM(MaterialColor.CRIMSON_STEM),
	CRIMSON_HYPHAE(MaterialColor.CRIMSON_HYPHAE),
	WARPED_NYLIUM(MaterialColor.WARPED_NYLIUM),
	WARPED_STEM(MaterialColor.WARPED_STEM),
	WARPED_HYPHAE(MaterialColor.WARPED_HYPHAE),
	WARPED_WART_BLOCK(MaterialColor.WARPED_WART_BLOCK),
	DEEPSLATE(MaterialColor.DEEPSLATE),
	RAW_IRON(MaterialColor.RAW_IRON);

	private final MaterialColor nms;

	MapColours(@Nonnull final MaterialColor nms) {
		this.nms = Objects.requireNonNull(nms);
	}

	public MaterialColor asNMS() {
		return this.nms;
	}

	public static void init() {
		final Set<MaterialColor> cmcMapColours = Stream.of(values())
				.map(MapColours::asNMS)
				.collect(Collectors.toSet());
		final Set<MaterialColor> nmsMapColours = Stream.of(MaterialColor.MATERIAL_COLORS)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		final Collection<MaterialColor> missingColours = CollectionUtils.disjunction(cmcMapColours, nmsMapColours);
		if (!missingColours.isEmpty()) {
			final CivLogger logger = CivLogger.getLogger(MapColours.class);
			logger.warning("The following map colours are missing: " + missingColours.stream()
					/** {@link MaterialMapColor#MaterialMapColor(int, int)} "id" parameter */
					.map(colour -> Integer.toString(colour.col))
					.collect(Collectors.joining(",")));
		}
	}

}
