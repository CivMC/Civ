package net.civmc.heliodor.heliodor.infusion.chunkmeta;

import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;

public class CauldronInfusion extends TableBasedDataObject {

    private int charge;
    private final int maxCharge;
    private int ticks;

    public CauldronInfusion(Location location, boolean isNew, int charge, int maxCharge) {
        super(location, isNew);
        this.charge = charge;
        this.maxCharge = maxCharge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public int getCharge() {
        return charge;
    }

    public int getMaxCharge() {
        return maxCharge;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public int getTicks() {
        return ticks;
    }
}
