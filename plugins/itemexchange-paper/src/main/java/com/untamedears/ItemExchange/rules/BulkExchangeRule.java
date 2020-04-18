package com.untamedears.itemexchange.rules;

import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializable;
import vg.civcraft.mc.civmodcore.serialization.NBTSerialization;

public final class BulkExchangeRule extends ExchangeData {

	public BulkExchangeRule() {
		this.nbt.setInteger("version", 3);
	}

    @Override
    public boolean isValid() {
        if (getRules().isEmpty()) {
            return false;
        }
        return true;
    }

    public List<ExchangeRule> getRules() {
        return Arrays.stream(this.nbt.getCompoundArray("rules")).
                map(NBTSerialization::deserialize).
                filter((serializable) -> serializable instanceof ExchangeRule).
                map((serializable) -> (ExchangeRule) serializable).
                collect(Collectors.toCollection(ArrayList::new));
    }

    public void setRules(List<ExchangeRule> rules) {
        if (rules == null) {
            this.nbt.remove("rules");
        }
        else {
            this.nbt.setCompoundArray("rules", rules.stream().
                    filter(Objects::nonNull).
                    map(NBTSerialization::serialize).
                    toArray(NBTCompound[]::new));
        }
    }

    @Override
    public void trace(ItemStack item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean conforms(ItemStack item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getDisplayedInfo() {
        return new ArrayList<>();
    }

    public ItemStack toItem() {
		List<ExchangeRule> rules = getRules();
        ItemStack item = NBTCompound.processItem(ItemExchangePlugin.RULE_ITEM.clone(), (nbt) ->
                nbt.setCompound("BulkExchangeRule", NBTSerialization.serialize(this)));
        ItemAPI.handleItemMeta(item, (ItemMeta meta) -> {
            meta.setDisplayName(ChatColor.RED + "Bulk Rule Block");
            meta.setLore(Collections.singletonList(
                    "This rule block holds " + rules.size() + " exchange rule" + (rules.size() == 1 ? "" : "s") + "."));
            return true;
        });
        return item;
    }

    public static BulkExchangeRule fromItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        if (item.getType() != ItemExchangePlugin.RULE_ITEM.getType()) {
            return null;
        }
        NBTCompound nbt = NBTCompound.fromItem(item).getCompound("BulkExchangeRule");
        if (nbt.isEmpty()) {
        	return null;
		}
		NBTSerializable serializable = NBTSerialization.deserialize(nbt);
		if (serializable instanceof BulkExchangeRule) {
			return (BulkExchangeRule) serializable;
		}
        return null;
    }

}
