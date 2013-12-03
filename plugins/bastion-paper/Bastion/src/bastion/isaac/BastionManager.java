package bastion.isaac;

import java.util.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;


public class BastionManager
{
  private Vector<BastionBlock> bastions;

  public BastionManager()
  {
    bastions = new Vector<BastionBlock>();
  }

  public void addBastion(Location location, int strength, Player p) {
    bastions.add(new BastionBlock(location, strength, p));
    Bastion.getPlugin().getLogger().info("bastion added");
  }
  public void handleBlockPlace(BlockPlaceEvent event) {
    for (BastionBlock bastion : bastions)
      if (bastion.blocked(event))
        bastion.handlePlaced(event.getBlock());
  }
}