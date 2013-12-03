package bastion.isaac;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.entity.IReinforcement;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

@SuppressWarnings("unused")
public class BastionBlock
{
  private Location location;
  private Player player;
  public int strength;

  BastionBlock(Location nLocation, int nStrength, Player nPlacer)
  {
    player = nPlacer;
    strength = nStrength;
    location = nLocation;
  }

  boolean blocked(BlockPlaceEvent event)
  {
    Bastion.getPlugin().getLogger().info("block placed");

    if (((event.getBlock().getX() - location.getX()) * (event.getBlock().getX() - location.getX()) + 
      (event.getBlock().getZ() - location.getZ()) * (event.getBlock().getZ() - location.getZ()) > 
      25.0D) || (event.getBlock().getY() <= location.getY())) {
      Bastion.getPlugin().getLogger().info("not blocked");
      return false;
    }
    Bastion.getPlugin().getLogger().info("blocked");
    return true;
  }
  void handlePlaced(Block block) {
    if (strength > 0) {
      block.breakNaturally();

      strength -= 1;
      IReinforcement old_reinforcement = Citadel.getReinforcementManager().getReinforcement(location.getBlock());

      if (old_reinforcement != null) {
        old_reinforcement.setDurability(strength);
        Citadel.getReinforcementManager().addReinforcement(old_reinforcement);
      }

    }

    Bastion.getPlugin().getLogger().info("strength=" + strength);
  }
}