/**
 * Created by Aleksey on 15.08.2017.
 */

package isaac.bastion.listeners;

import isaac.bastion.storage.BastionBlockStorage;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;

public class NameLayerListener implements Listener {

    private BastionBlockStorage storage;

    public NameLayerListener(BastionBlockStorage storage) {
        this.storage = storage;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGroupDeleteEvent(GroupDeleteEvent event) {
        List<Integer> groupIds = event.getGroup().getGroupIds();

        this.storage.removeGroups(groupIds);
    }
}
