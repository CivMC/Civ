package vg.civcraft.mc.civmodcore.legacy.materials;

import org.bukkit.Material;

public interface IMaterial {

    /**
     * Gets the Bukkit material version of this.
     *
     * @return Returns the Bukkit material.
     *
     * @apiNote MUST BE CALLING FROM A SERVER VERSION THAT MATCHES THE MATERIAL IMPLEMENTATION, IN THAT YOU SHOULD ONLY
     * CALL THIS METHOD ON A 1.12 MATERIAL FROM A 1.12 SERVER OTHERWISE IT WILL NOT WORK CORRECTLY!
     */
    Material getMaterial();

}
