package com.untamedears.itemexchange.rules.interfaces;

import java.util.List;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializable;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializationException;

/**
 * This class forms the basis of all exchange data.
 */
public abstract class ExchangeData extends BaseRule implements NBTSerializable {

    protected final NBTCompound nbt = new NBTCompound();

    /**
     * @return Returns true if this exchange data is valid.
     */
    public abstract boolean isValid();

    /**
     * Base the exchange data on the given item.
     *
     * @param item The item to base this exchange data on.
     */
    public abstract void trace(ItemStack item);

    /**
     * Checks if an arbitrary item conforms to this exchange data's requirements.
     *
     * @param item The arbitrary item to check.
     * @return Returns true if the given item conforms.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean conforms(ItemStack item);

    /**
     * Generate data to be displayed.
     *
     * @return Returns any display information.
     *
     * @apiNote This information will be displayed in chat <i>AND</i> on items as law. Ensure you format correctly.
     */
    public abstract List<String> getDisplayedInfo();

    @Override
    public final void serialize(NBTCompound nbt) throws NBTSerializationException {
        nbt.adopt(this.nbt);
    }

    @Override
    public final void deserialize(NBTCompound nbt) throws NBTSerializationException {
        checkLocked();
        this.nbt.adopt(nbt);
    }

    /**
     * @return Returns the base NBT data held by this exchange data.
     */
    public final NBTCompound getNBT() {
        try {
            return this.nbt.clone();
        }
        catch (CloneNotSupportedException ignored) {
            return new NBTCompound();
        }
    }

}
