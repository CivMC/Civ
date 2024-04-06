package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.destroystokyo.paper.MaterialTags;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.HorseArmourConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public final class HorseArmour extends SimpleHack<HorseArmourConfig> implements Listener {
    @ApiStatus.Internal
    public HorseArmour(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull HorseArmourConfig config
    ) {
        super(plugin, config);
    }

    @ApiStatus.Internal
    public static @NotNull HorseArmourConfig generate(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull ConfigurationSection config
    ) {
        return new HorseArmourConfig(plugin, config);
    }

    private static final List<ShapedRecipe> RECIPES = List.of(
        createHorseArmourRecipe("iron_plated_horse_armour", Material.IRON_INGOT, Material.IRON_HORSE_ARMOR),
        createHorseArmourRecipe("gold_plated_horse_armour", Material.GOLD_INGOT, Material.GOLDEN_HORSE_ARMOR),
        createHorseArmourRecipe("diamond_plated_horse_armour", Material.DIAMOND, Material.DIAMOND_HORSE_ARMOR)
    );

    @Override
    public void onEnable() {
        super.onEnable();
        if (config().shouldRegisterCraftingRecipes()) {
            RECIPES.forEach(Bukkit::addRecipe);
        }
        plugin().registerListener(this);
    }

    @Override
    public void onDisable() {
        for (final ShapedRecipe recipe : RECIPES) {
            Bukkit.removeRecipe(recipe.getKey());
        }
        HandlerList.unregisterAll(this);
        super.onDisable();
    }

    @EventHandler(ignoreCancelled = true)
    private void reimplementDurability(
        final @NotNull EntityDamageEvent event
    ) {
        // Do nothing if this feature isn't enabled
        if (!config().reimplementDurability()) {
            return;
        }

        // Do nothing if the entity is not a horse
        if (!(event.getEntity() instanceof final Horse horse)) {
            return;
        }

        // Do nothing if the horse is not wearing armour
        final ItemStack armourItem = horse.getInventory().getArmor();
        if (ItemUtils.isEmptyItem(armourItem)) {
            return;
        }
        final Material armourMaterial = armourItem.getType();
        if (!MaterialTags.HORSE_ARMORS.isTagged(armourMaterial)) {
            return;
        }

        // Do nothing if the armour didn't mitigate any of the damage
        @SuppressWarnings("deprecation")
        final double damageMitigated = event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) * -1;
        if (damageMitigated <= 0) {
            return;
        }

        final ItemMeta armourMeta = armourItem.getItemMeta();
        if (!(armourMeta instanceof final Damageable damageableMeta)) {
            this.logger.warning("Horse armour could not be cast to Damageable!");
            return;
        }

        // Reimplement Unbreaking
        if (config().reimplementUnbreaking()) {
            final int unbreakingLevel = armourMeta.getEnchantLevel(Enchantment.UNBREAKING);
            if (unbreakingLevel > 0) {
                // https://minecraft.fandom.com/wiki/Unbreaking#Usage
                final double chanceOfReducingDurability = (40d / (unbreakingLevel + 1d)) + 60d;
                if (ThreadLocalRandom.current().nextDouble(0, 100) >= chanceOfReducingDurability) {
                    return;
                }
            }
        }

        final int maxDurability;
        if (damageableMeta.hasMaxDamage()) {
            maxDurability = damageableMeta.getMaxDamage();
        }
        else {
            switch (armourMaterial) {
                case LEATHER_HORSE_ARMOR -> maxDurability = Material.LEATHER_CHESTPLATE.getMaxDurability();
                case IRON_HORSE_ARMOR -> maxDurability = Material.IRON_CHESTPLATE.getMaxDurability();
                case GOLDEN_HORSE_ARMOR -> maxDurability = Material.GOLDEN_CHESTPLATE.getMaxDurability();
                case DIAMOND_HORSE_ARMOR -> maxDurability = Material.DIAMOND_CHESTPLATE.getMaxDurability();
                default -> {
                    // Just in case another type of horse armour gets added
                    this.logger.warning("New kind of horse armour! Add it to the HorseArmour SAH!");
                    return;
                }
            }
            damageableMeta.setMaxDamage(maxDurability);
        }

        // Break the item if durability is fully expended
        int currentDurability = damageableMeta.getDamage();
        if (++currentDurability >= maxDurability) {
            /** {@link net.minecraft.world.entity.LivingEntity#breakItem(net.minecraft.world.item.ItemStack)} */
            horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
            horse.getWorld().spawnParticle(Particle.ITEM, horse.getLocation(), 5, armourItem);
            horse.getInventory().setArmor(null);
            return;
        }
        damageableMeta.setDamage(currentDurability);

        armourItem.setItemMeta(armourMeta);
    }

    private static @NotNull ShapedRecipe createHorseArmourRecipe(
        final @NotNull String slug,
        final @NotNull Material ingredient,
        final @NotNull Material result
    ) {
        final var recipe = new ShapedRecipe(
            new NamespacedKey("sah", slug),
            new ItemStack(result)
        );
        recipe.shape(
            "i i",
            "iai",
            "i i"
        );
        recipe.setIngredient('i', ingredient);
        recipe.setIngredient('a', Material.LEATHER_HORSE_ARMOR);
        return recipe;
    }
}
