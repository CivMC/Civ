package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.util.JAUtility;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

public abstract class LoggableBlockAction extends LoggablePlayerAction {

    protected static final Logger LOGGER = CivLogger.getLogger(LoggableBlockAction.class);

    protected final Location blockLocation;
    protected final Material blockMaterial;

    public LoggableBlockAction(
        final long timestamp,
        final @NotNull Snitch snitch,
        final @NotNull UUID player,
        final @NotNull Location blockLocation,
        final @NotNull Material blockMaterial
    ) {
        super(timestamp, snitch, player);
        this.blockLocation = blockLocation;
        this.blockMaterial = Objects.requireNonNullElse(blockMaterial, Material.AIR); // Just in case
    }

    /**
     * @return The location of the block in question.
     */
    public @NotNull Location getLocation() {
        return this.blockLocation;
    }

    /**
     * @return The material of the block in question.
     */
    public @NotNull Material getMaterial() {
        return this.blockMaterial;
    }

    @Override
    public @NotNull LoggedActionPersistence getPersistence() {
        return new LoggedActionPersistence(
            getPlayer(),
            getLocation(),
            getTime(),
            getMaterial().name()
        );
    }

    @Override
    public @NotNull IClickable getGUIRepresentation() {
        final Material blockMaterial = getMaterial();
        final ItemStack guiRepresentation = new ItemStack(switch (blockMaterial) {
            // Signs
            case OAK_WALL_SIGN -> Material.OAK_SIGN;
            case DARK_OAK_WALL_SIGN -> Material.DARK_OAK_SIGN;
            case BIRCH_WALL_SIGN -> Material.BIRCH_SIGN;
            case SPRUCE_WALL_SIGN -> Material.SPRUCE_SIGN;
            case JUNGLE_WALL_SIGN -> Material.JUNGLE_SIGN;
            case ACACIA_WALL_SIGN -> Material.ACACIA_SIGN;
            case WARPED_WALL_SIGN -> Material.WARPED_SIGN;
            case CRIMSON_WALL_SIGN -> Material.CRIMSON_SIGN;

            // Torches
            case WALL_TORCH -> Material.TORCH;
            case SOUL_WALL_TORCH -> Material.SOUL_TORCH;
            case REDSTONE_WALL_TORCH -> Material.REDSTONE_TORCH;

            // Banners
            case WHITE_WALL_BANNER -> Material.WHITE_BANNER;
            case BLACK_WALL_BANNER -> Material.BLACK_BANNER;
            case BLUE_WALL_BANNER -> Material.BLUE_BANNER;
            case BROWN_WALL_BANNER -> Material.BROWN_BANNER;
            case CYAN_WALL_BANNER -> Material.CYAN_BANNER;
            case GRAY_WALL_BANNER -> Material.GRAY_BANNER;
            case GREEN_WALL_BANNER -> Material.GREEN_BANNER;
            case LIGHT_BLUE_WALL_BANNER -> Material.LIGHT_BLUE_BANNER;
            case LIGHT_GRAY_WALL_BANNER -> Material.LIGHT_GRAY_BANNER;
            case LIME_WALL_BANNER -> Material.LIME_BANNER;
            case MAGENTA_WALL_BANNER -> Material.MAGENTA_BANNER;
            case ORANGE_WALL_BANNER -> Material.ORANGE_BANNER;
            case PINK_WALL_BANNER -> Material.PINK_BANNER;
            case PURPLE_WALL_BANNER -> Material.PURPLE_BANNER;
            case RED_WALL_BANNER -> Material.RED_BANNER;
            case YELLOW_WALL_BANNER -> Material.YELLOW_BANNER;

            // Heads
            case DRAGON_WALL_HEAD -> Material.DRAGON_HEAD;
            case PLAYER_WALL_HEAD -> Material.PLAYER_HEAD;
            case ZOMBIE_WALL_HEAD -> Material.ZOMBIE_HEAD;
            case CREEPER_WALL_HEAD -> Material.CREEPER_HEAD;
            case SKELETON_WALL_SKULL -> Material.SKELETON_SKULL;
            case WITHER_SKELETON_WALL_SKULL -> Material.WITHER_SKELETON_SKULL;

            // Machines
            case PISTON_HEAD, MOVING_PISTON -> Material.PISTON;
            case REDSTONE_WIRE -> Material.REDSTONE;
            case TRIPWIRE -> Material.STRING;

            // Cauldron (liquid is lost)
            case WATER_CAULDRON, LAVA_CAULDRON, POWDER_SNOW_CAULDRON -> Material.CAULDRON;

            // Potted Plants (plant is lost)
            case POTTED_OAK_SAPLING, POTTED_SPRUCE_SAPLING, POTTED_BIRCH_SAPLING, POTTED_JUNGLE_SAPLING,
                 POTTED_ACACIA_SAPLING, POTTED_DARK_OAK_SAPLING, POTTED_FERN, POTTED_DANDELION, POTTED_POPPY,
                 POTTED_BLUE_ORCHID, POTTED_ALLIUM, POTTED_AZURE_BLUET, POTTED_RED_TULIP, POTTED_ORANGE_TULIP,
                 POTTED_WHITE_TULIP, POTTED_PINK_TULIP, POTTED_OXEYE_DAISY, POTTED_CORNFLOWER,
                 POTTED_LILY_OF_THE_VALLEY, POTTED_WITHER_ROSE, POTTED_RED_MUSHROOM, POTTED_BROWN_MUSHROOM,
                 POTTED_DEAD_BUSH, POTTED_CACTUS, POTTED_BAMBOO, POTTED_CRIMSON_FUNGUS, POTTED_WARPED_FUNGUS,
                 POTTED_CRIMSON_ROOTS, POTTED_WARPED_ROOTS, POTTED_AZALEA_BUSH, POTTED_FLOWERING_AZALEA_BUSH ->
                Material.FLOWER_POT;

            // Plants
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT;
            case COCOA -> Material.COCOA_BEANS;
            case ATTACHED_PUMPKIN_STEM, PUMPKIN_STEM -> Material.PUMPKIN_SEEDS;
            case ATTACHED_MELON_STEM, MELON_STEM -> Material.MELON_SEEDS;
            case TALL_SEAGRASS -> Material.SEAGRASS;
            case SWEET_BERRY_BUSH -> Material.SWEET_BERRIES;
            case KELP_PLANT -> Material.KELP;
            case BAMBOO_SAPLING -> Material.BAMBOO;
            case WEEPING_VINES_PLANT -> Material.WEEPING_VINES;
            case TWISTING_VINES_PLANT -> Material.TWISTING_VINES;
            case CAVE_VINES, CAVE_VINES_PLANT -> Material.GLOW_BERRIES;
            case BIG_DRIPLEAF_STEM -> Material.BIG_DRIPLEAF;

            // Coral
            case TUBE_CORAL_WALL_FAN -> Material.TUBE_CORAL_FAN;
            case BRAIN_CORAL_WALL_FAN -> Material.BRAIN_CORAL_FAN;
            case BUBBLE_CORAL_WALL_FAN -> Material.BUBBLE_CORAL_FAN;
            case FIRE_CORAL_WALL_FAN -> Material.FIRE_CORAL_FAN;
            case HORN_CORAL_WALL_FAN -> Material.HORN_CORAL_FAN;
            // Dead Coral
            case DEAD_TUBE_CORAL_WALL_FAN -> Material.DEAD_TUBE_CORAL_FAN;
            case DEAD_BRAIN_CORAL_WALL_FAN -> Material.DEAD_BRAIN_CORAL_FAN;
            case DEAD_BUBBLE_CORAL_WALL_FAN -> Material.DEAD_BUBBLE_CORAL_FAN;
            case DEAD_FIRE_CORAL_WALL_FAN -> Material.DEAD_FIRE_CORAL_FAN;
            case DEAD_HORN_CORAL_WALL_FAN -> Material.DEAD_HORN_CORAL_FAN;

            // Candle Cakes (candle is lost)
            case CANDLE_CAKE, WHITE_CANDLE_CAKE, ORANGE_CANDLE_CAKE, MAGENTA_CANDLE_CAKE, LIGHT_BLUE_CANDLE_CAKE,
                 YELLOW_CANDLE_CAKE, LIME_CANDLE_CAKE, PINK_CANDLE_CAKE, GRAY_CANDLE_CAKE, LIGHT_GRAY_CANDLE_CAKE,
                 CYAN_CANDLE_CAKE, PURPLE_CANDLE_CAKE, BLUE_CANDLE_CAKE, BROWN_CANDLE_CAKE, GREEN_CANDLE_CAKE,
                 RED_CANDLE_CAKE, BLACK_CANDLE_CAKE -> Material.CAKE;

            // Unobtainable (substituted)
            case WATER -> Material.WATER_BUCKET;
            case LAVA -> Material.LAVA_BUCKET;
            case POWDER_SNOW -> Material.POWDER_SNOW_BUCKET;
            case FIRE, SOUL_FIRE -> Material.FLINT_AND_STEEL;
            case FROSTED_ICE -> Material.ICE; // More info: https://minecraft.fandom.com/wiki/Frosted_Ice

            // Just in case
            case AIR -> Material.BARRIER;

            default -> {
                if (blockMaterial.isItem()) {
                    yield blockMaterial;
                }
                LOGGER.warning("Could not represent [" + blockMaterial.name() + "] as a GUI item, please add it to the switch!");
                yield Material.STONE;
            }
        });
        guiRepresentation.editMeta((meta) -> {
            MetaUtils.addComponentLore(
                meta,
                Component.text().append(
                    Component.text("Material: ", NamedTextColor.GOLD),
                    Component.translatable(getMaterial(), NamedTextColor.AQUA)
                ).build()
            );
        });
        super.enrichGUIItem(guiRepresentation);
        ItemUtils.addLore(guiRepresentation, ChatColor.GOLD + JAUtility.formatLocation(getLocation(), false));
        return new DecorationStack(guiRepresentation);
    }

    @Override
    protected @NotNull Location getLocationForStringRepresentation() {
        return getLocation();
    }
}
