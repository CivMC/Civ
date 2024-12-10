package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.interfaces.DisplayContext;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "RECEIPT", order = Short.MAX_VALUE)
public final class ReceiptModifier extends ModifierData {
    public static final ReceiptModifier TEMPLATE = new ReceiptModifier();

    @Override
    public @Nullable ReceiptModifier construct(
        final @NotNull ItemStack item
    ) {
        return null; // Receipts are not a default modifier
    }

    @Override
    public boolean isBroken() {
        return false;
    }

    @Override
    public boolean conforms(
        final @NotNull ItemStack item
    ) {
        return true; // Have no effect on the matching of the item
    }

    @Override
    public String getDisplayListing() {
        return null; // Have no effect on listing names
    }

    private static final String FORCE_RECEIPT_GEN_KEY = "force_receipt";
    private static final String FOOTER_TEXT_KEY = "force_receipt";

    public boolean forceReceiptGeneration;
    public String footerText;

    @Override
    public void toNBT(
        final @NotNull NBTCompound nbt
    ) {
        nbt.setBoolean(FORCE_RECEIPT_GEN_KEY, this.forceReceiptGeneration);
        nbt.setString(FOOTER_TEXT_KEY, this.footerText);
    }

    public static @NotNull ReceiptModifier fromNBT(
        final @NotNull NBTCompound nbt
    ) {
        final var modifier = new ReceiptModifier();
        modifier.forceReceiptGeneration = nbt.getBoolean(FORCE_RECEIPT_GEN_KEY);
        modifier.footerText = nbt.getNullableString(FOOTER_TEXT_KEY);
        return modifier;
    }

    @Override
    public List<String> getDisplayInfo(
        final @NotNull DisplayContext context
    ) {
        final var info = new ArrayList<String>();
        if (this.forceReceiptGeneration) {
            info.add(ChatColor.GOLD + "Will produce a receipt regardless.");
        }
        if (context == DisplayContext.BUTTON_LORE) {
            if (StringUtils.isNotBlank(this.footerText)) {
                info.add(ChatColor.GOLD + "Footer: " + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + this.footerText);
            }
        }
        return info;
    }
}
