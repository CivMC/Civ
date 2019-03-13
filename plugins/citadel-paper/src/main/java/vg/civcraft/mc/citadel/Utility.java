package vg.civcraft.mc.citadel;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
/**
 * Just a useful class with general and misplaced methods that can be
 * called from anywhere.
 *
 */
public class Utility {

    public static void sendAndLog(CommandSender receiver, ChatColor color, String message) {
        receiver.sendMessage(color + message);
        if (Citadel.getInstance().getConfigManager().logMessages()) {
            Citadel.getInstance().getLogger().log(Level.INFO, "Sent {0} reply {1}", new Object[]{receiver.getName(), message});
        }
    }
    
    private static boolean isSoilPlant(Material mat) {
        return Material.WHEAT.equals(mat)
            || Material.MELON_STEM.equals(mat)
            || Material.PUMPKIN_STEM.equals(mat)
            || Material.CARROT.equals(mat)
            || Material.POTATO.equals(mat)
            || Material.CROPS.equals(mat)
            || Material.MELON_BLOCK.equals(mat)
            || Material.PUMPKIN.equals(mat)
			|| Material.BEETROOT_BLOCK.equals(mat);
    }

    private static boolean isDirtPlant(Material mat) {
        return Material.SUGAR_CANE_BLOCK.equals(mat)
            || Material.MELON_BLOCK.equals(mat)
            || Material.PUMPKIN.equals(mat);
    }

    private static boolean isGrassPlant(Material mat) {
        return Material.SUGAR_CANE_BLOCK.equals(mat)
            || Material.MELON_BLOCK.equals(mat)
            || Material.PUMPKIN.equals(mat);
    }

    private static boolean isSandPlant(Material mat) {
        return Material.CACTUS.equals(mat)
            || Material.SUGAR_CANE_BLOCK.equals(mat);
    }

    private static boolean isSoulSandPlant(Material mat) {
        return Material.NETHER_WARTS.equals(mat);
    }

    public static boolean isPlant(Block plant) {
        return isPlant(plant.getType());
    }

    public static boolean isPlant(Material mat) {
        return isSoilPlant(mat)
            || isDirtPlant(mat)
            || isGrassPlant(mat)
            || isSandPlant(mat)
            || isSoulSandPlant(mat);
    }


	/**
	 * A better version of dropNaturally that mimics normal drop behavior.
	 *
	 * The built-in version of Bukkit's dropItem() method places the item at the block
	 * vertex which can make the item jump around.
	 * This method places the item in the middle of the block location with a slight
	 * vertical velocity to mimic how normal broken blocks appear.
	 * @param l The location to drop the item
	 * @param is The item to drop
	 *
	 * @author GordonFreemanQ
	 */
	public static void dropItemAtLocation(final Location l, final ItemStack is) {
		// Schedule the item to drop 1 tick later
		Bukkit.getScheduler().scheduleSyncDelayedTask(Citadel.getInstance(), new Runnable() {
			@Override
			public void run() {
				try {
					l.getWorld().dropItem(l.add(0.5, 0.5, 0.5), is).setVelocity(new Vector(0, 0.05, 0));
				} catch (Exception e) {
					Citadel.getInstance().getLogger().log(Level.WARNING,
								"Utility dropItemAtLocation called but errored: ", e);
				}
			}
		}, 1);
	}


	/**
	 * Overload for dropItemAtLocation(Location l, ItemStack is) that accepts a block parameter.
	 * @param b The block to drop it at
	 * @param is The item to drop
	 *
	 * @author GordonFreemanQ
	 */
	public static void dropItemAtLocation(Block b, ItemStack is) {
    	if (b == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility dropItemAtLocation block called with null");
			return;
		}
		dropItemAtLocation(b.getLocation(), is);
	}
}
