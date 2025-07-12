package vg.civcraft.mc.civmodcore.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.world.model.EllipseArea;
import vg.civcraft.mc.civmodcore.world.model.GlobalYLimitedArea;
import vg.civcraft.mc.civmodcore.world.model.IArea;
import vg.civcraft.mc.civmodcore.world.model.RectangleArea;

public final class ConfigHelper {

    private static final Logger LOGGER = Bukkit.getLogger();

    /**
     * Retrieves the configuration section at the given key on the given configuration section.
     *
     * @param config The config to get the keyed section from.
     * @param key    The key of the section to retrieve.
     * @return Returns the configuration section at the given key, or returns a new, empty section.
     */
    @NotNull
    public static ConfigurationSection getSection(@NotNull final ConfigurationSection config,
                                                  @NotNull final String key) {
        ConfigurationSection found = config.getConfigurationSection(key);
        if (found == null) {
            found = config.createSection(key);
        }
        return found;
    }

    /**
     * Retrieves a string list from a given config section. If the keyed value is a standalone string instead of a
     * list, that value will be converted to a list.
     *
     * @param config The config section to retrieve the list from.
     * @param key    The key to get the list of.
     * @return Returns a list of strings, which is never null.
     */
    @NotNull
    public static List<String> getStringList(@NotNull final ConfigurationSection config,
                                             @NotNull final String key) {
        if (config.isString(key)) {
            final var list = new ArrayList<String>(1);
            list.add(config.getString(key));
            return list;
        }
        return config.getStringList(key);
    }

    /**
     * Attempts to retrieve a list from a config section.
     *
     * @param <T>    The type to parse the list into.
     * @param config The config section.
     * @param key    The key of the list.
     * @param parser The parser to convert the string value into the correct type.
     * @return Returns a list, or null.
     */
    @NotNull
    public static <T> List<T> parseList(@NotNull final ConfigurationSection config,
                                        @NotNull final String key,
                                        @NotNull final Function<String, T> parser) {
        if (!config.isList(key)) {
            return new ArrayList<>(0);
        }
        final var entries = getStringList(config, key);
        final var result = new ArrayList<T>(entries.size());
        for (final String entry : entries) {
            final T item = parser.apply(entry);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Attempts to retrieve a list of materials from a config section.
     *
     * @param config The config section.
     * @param key    The key of the list.
     * @return Returns a list of materials, or null.
     */
    @NotNull
    public static List<Material> parseMaterialList(@NotNull final ConfigurationSection config,
                                                   @NotNull final String key) {
        return parseList(config, key, MaterialUtils::getMaterial);
    }

    /**
     * Creates an item map containing all the items listed in the given config
     * section
     *
     * @param config ConfigurationSection to parse the items from
     * @return The item map created
     */
    @NotNull
    public static ItemMap parseItemMap(@Nullable final ConfigurationSection config) {
        final var result = new ItemMap();
        if (config == null) {
            return result;
        }
        for (final String key : config.getKeys(false)) {
            ItemMap partMap = new ItemMap();
            ConfigurationSection section = config.getConfigurationSection(key);
            String custom = section == null ? null : section.getString("custom-key");
            if (custom != null) {
                ItemStack item = CustomItem.getCustomItem(custom);
                if (item == null) {
                    throw new IllegalArgumentException("Unknown custom item key " + custom);
                } else {
                    int amount = section.getInt("amount", 1);
                    partMap.addItemAmount(item, amount);
                }
            } else {
                partMap.addItemStack(config.getItemStack(key, ItemStack.empty()));
            }
            result.merge(partMap);
        }
        return result;
    }

    public static int parseTimeAsTicks(@NotNull final String arg) {
        return (int) (parseTime(arg, TimeUnit.MILLISECONDS) / 50L);
    }

    public static long parseTime(@NotNull final String arg,
                                 @NotNull final TimeUnit unit) {
        long millis = parseTime(arg);
        return unit.convert(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Parses a time value specified in a config. This allows to specify human
     * readable time values easily, instead of having to specify every amount in
     * ticks or seconds. The unit of a number specifed by the letter added after it,
     * for example 5h means 5 hours or 34s means 34 seconds. Possible modifiers are:
     * t (ticks), s (seconds), m (minutes), h (hours) and d (days)
     * <p>
     * Additionally you can combine those amounts in any way you want, for example
     * you can specify 3h5m43s as 3 hours, 5 minutes and 43 seconds. This doesn't
     * have to be sorted and may even list the same unit multiple times for
     * different values, but the values are not allowed to be separated by anything
     *
     * @param input Parsed string containing the time format
     * @return How many milliseconds the given time value is
     */
    public static long parseTime(@NotNull String input) {
        input = input.replace(" ", "").replace(",", "").toLowerCase();
        long result = 0;
        try {
            result += Long.parseLong(input);
            return result;
        } catch (NumberFormatException e) {
        }
        while (!input.equals("")) {
            String typeSuffix = getSuffix(input, Character::isLetter);
            input = input.substring(0, input.length() - typeSuffix.length());
            String numberSuffix = getSuffix(input, Character::isDigit);
            input = input.substring(0, input.length() - numberSuffix.length());
            long duration;
            if (numberSuffix.length() == 0) {
                duration = 1;
            } else {
                duration = Long.parseLong(numberSuffix);
            }
            switch (typeSuffix) {
                case "ms":
                case "milli":
                case "millis":
                    result += duration;
                    break;
                case "s": // seconds
                case "sec":
                case "second":
                case "seconds":
                    result += TimeUnit.SECONDS.toMillis(duration);
                    break;
                case "m": // minutes
                case "min":
                case "minute":
                case "minutes":
                    result += TimeUnit.MINUTES.toMillis(duration);
                    break;
                case "h": // hours
                case "hour":
                case "hours":
                    result += TimeUnit.HOURS.toMillis(duration);
                    break;
                case "d": // days
                case "day":
                case "days":
                    result += TimeUnit.DAYS.toMillis(duration);
                    break;
                case "w": // weeks
                case "week":
                case "weeks":
                    result += TimeUnit.DAYS.toMillis(duration * 7);
                    break;
                case "month": // weeks
                case "months":
                    result += TimeUnit.DAYS.toMillis(duration * 30);
                    break;
                case "y":
                case "year":
                case "years":
                    result += TimeUnit.DAYS.toMillis(duration * 365);
                    break;
                case "never":
                case "inf":
                case "infinite":
                case "perm":
                case "perma":
                case "forever":
                    // 1000 years counts as perma
                    result += TimeUnit.DAYS.toMillis(365 * 1000);
                default:
                    // just ignore it
            }
        }
        return result;
    }

    @NotNull
    private static String getSuffix(@NotNull final String arg,
                                    @NotNull final Predicate<Character> selector) {
        StringBuilder number = new StringBuilder();
        for (int i = arg.length() - 1; i >= 0; i--) {
            if (selector.test(arg.charAt(i))) {
                number.insert(0, arg.substring(i, i + 1));
            } else {
                break;
            }
        }
        return number.toString();
    }

    @Nullable
    public static IArea parseArea(@Nullable final ConfigurationSection config) {
        if (config == null) {
            LOGGER.warning("Tried to parse area on null section");
            return null;
        }
        String type = config.getString("type");
        if (type == null) {
            LOGGER.warning("Found no area type at " + config.getCurrentPath());
            return null;
        }
        int lowerYBound = config.getInt("lowerYBound", 0);
        int upperYBound = config.getInt("upperYBound", 255);
        String worldName = config.getString("world");
        if (worldName == null) {
            LOGGER.warning("Found no world specified for area at " + config.getCurrentPath());
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            LOGGER.warning("Found no world with name " + worldName + " as specified at " + config.getCurrentPath());
            return null;
        }
        Location center = null;
        if (config.isConfigurationSection("center")) {
            ConfigurationSection centerSection = config.getConfigurationSection("center");
            int x = centerSection.getInt("x", 0);
            int y = centerSection.getInt("y", 0);
            int z = centerSection.getInt("z", 0);
            if (world != null) {
                center = new Location(world, x, y, z);
            }
        }
        int xSize = config.getInt("xSize", -1);
        int zSize = config.getInt("zSize", -1);
        IArea area = null;
        switch (type) {
            case "GLOBAL":
                area = new GlobalYLimitedArea(lowerYBound, upperYBound, world);
                break;
            case "ELLIPSE":
                if (center == null) {
                    LOGGER.warning("Found no center for area at " + config.getCurrentPath());
                    return null;
                }
                if (xSize == -1) {
                    LOGGER.warning("Found no xSize for area at " + config.getCurrentPath());
                    return null;
                }
                if (zSize == -1) {
                    LOGGER.warning("Found no zSize for area at " + config.getCurrentPath());
                    return null;
                }
                area = new EllipseArea(lowerYBound, upperYBound, center, xSize, zSize);
                break;
            case "RECTANGLE":
                if (center == null) {
                    LOGGER.warning("Found no center for area at " + config.getCurrentPath());
                    return null;
                }
                if (xSize == -1) {
                    LOGGER.warning("Found no xSize for area at " + config.getCurrentPath());
                    return null;
                }
                if (zSize == -1) {
                    LOGGER.warning("Found no zSize for area at " + config.getCurrentPath());
                    return null;
                }
                area = new RectangleArea(lowerYBound, upperYBound, center, xSize, zSize);
                break;
            default:
                LOGGER.warning("Invalid area type " + type + " at " + config.getCurrentPath());
        }
        return area;
    }

    /**
     * Parses a section which contains key-value mappings of a type to another type
     *
     * @param <K>            Key type
     * @param <V>            Value type
     * @param parent         Configuration section containing the section with the values
     * @param identifier     Config identifier of the section containing the entries
     * @param logger         The logger to write in progress work to
     * @param keyConverter   Converts strings to type K
     * @param valueConverter Converts strings to type V
     * @param mapToUse       The map to place parsed keys and values.
     */
    public static <K, V> void parseKeyValueMap(@NotNull final ConfigurationSection parent,
                                               @NotNull final String identifier,
                                               @NotNull final Logger logger,
                                               @NotNull final Function<String, K> keyConverter,
                                               @NotNull final Function<String, V> valueConverter,
                                               @NotNull final Map<K, V> mapToUse) {
        if (!parent.isConfigurationSection(identifier)) {
            return;
        }
        ConfigurationSection section = parent.getConfigurationSection(identifier);
        for (String keyString : section.getKeys(false)) {
            if (section.isConfigurationSection(keyString)) {
                logger.warning(
                    "Ignoring invalid " + identifier + " entry " + keyString + " at " + section.getCurrentPath());
                continue;
            }
            K keyinstance;
            try {
                keyinstance = keyConverter.apply(keyString);
            } catch (IllegalArgumentException e) {
                logger.warning("Failed to parse " + identifier + " " + keyString + " at " + section.getCurrentPath()
                    + ": " + e.toString());
                continue;
            }
            V value = valueConverter.apply(section.getString(keyString));
            mapToUse.put(keyinstance, value);
        }
    }
}
