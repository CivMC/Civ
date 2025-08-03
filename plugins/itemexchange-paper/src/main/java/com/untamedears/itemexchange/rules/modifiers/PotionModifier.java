package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.NBTEncodings;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;
import vg.civcraft.mc.civmodcore.utilities.NullUtils;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "POTION", order = 400)
public final class PotionModifier extends ModifierData {

    public static final PotionModifier TEMPLATE = new PotionModifier();

    public static final String BASE_KEY = "base";
    public static final String EFFECTS_KEY = "effects";

    private PotionType base;
    private List<PotionEffect> effects;
    private boolean splash;

    @Override
    public PotionModifier construct(ItemStack item) {
        if (!(item.getItemMeta() instanceof final PotionMeta meta) || meta.getBasePotionType() == null) {
            return null;
        }
        PotionModifier modifier = new PotionModifier();
        modifier.base = meta.getBasePotionType();
        modifier.effects = meta.getCustomEffects();
        modifier.splash = item.getType() == Material.SPLASH_POTION;
        return modifier;
    }

    @Override
    public boolean isBroken() {
        if (this.base == null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean conforms(ItemStack item) {
        if (!(item.getItemMeta() instanceof final PotionMeta meta)) {
            return false;
        }
        if (!NullUtils.equalsNotNull(this.base, meta.getBasePotionType())) {
            return false;
        }
        List<PotionEffect> heldEffects = getEffects();
        List<PotionEffect> metaEffects = meta.getCustomEffects();
        if (metaEffects.size() != heldEffects.size()) {
            return false;
        }
        if (!metaEffects.containsAll(heldEffects)) {
            return false;
        }
        return true;
    }

    @Override
    public void toNBT(@NotNull final NbtCompound nbt) {
        nbt.setCompound(BASE_KEY, NBTEncodings.encodePotionData(this.base));
        nbt.setCompoundArray(EFFECTS_KEY, getEffects().stream()
            .map(NBTEncodings::encodePotionEffect)
            .toArray(NbtCompound[]::new));
    }

    public static PotionModifier fromNBT(@NotNull final NbtCompound nbt) {
        final var modifier = new PotionModifier();
        PotionType type = NBTEncodings.decodePotionData(nbt.getCompound(BASE_KEY, true));
        if (type == null) {
            return null; // "UNCRAFTABLE" potion which is removed in 1.21
        }
        modifier.setPotionData(type);
        modifier.setEffects(Arrays.stream(nbt.getCompoundArray(EFFECTS_KEY, true))
            .map(NBTEncodings::decodePotionEffect)
            .collect(Collectors.toCollection(ArrayList::new)));
        return modifier;
    }

    @Override
    public String getDisplayListing() {
        String listing = getName();
        if (Strings.isNullOrEmpty(listing)) {
            return null;
        }
        return ChatColor.WHITE + listing;
    }

    @Override
    public List<String> getDisplayInfo() {
        return Collections.singletonList(ChatColor.AQUA + "Potion Name: " + ChatColor.WHITE + getName());
    }

    @Override
    public String toString() {
        return getSlug() +
            "{" +
            "base=" + Utilities.potionDataToString(getPotionType()) + "," +
            "effects=" + Utilities.potionEffectsToString(getEffects()) + "," +
            "}";
    }

    // ------------------------------------------------------------
    // Getters + Setters
    // ------------------------------------------------------------

    public String getName() {
        if (this.base == null) {
            return null;
        }
        return (splash ? "Splash " : "")
            + WordUtils.capitalize(this.base.getKey().getKey().replace("_", " "));
    }

    public PotionType getPotionType() {
        if (this.base == null) {
            return PotionType.WATER;
        }
        return this.base;
    }

    public void setPotionData(PotionType data) {
        this.base = data;
    }

    public List<PotionEffect> getEffects() {
        if (this.effects == null) {
            return Lists.newArrayList();
        }
        return this.effects;
    }

    public void setEffects(List<PotionEffect> effects) {
        this.effects = effects;
    }

}
