package net.civmc.kiragatewayvelocity.rabbit;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.civmc.kiragatewayvelocity.KiraGateway;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.InheritanceNode;
import org.spongepowered.configurate.CommentedConfigurationNode;

public class PatreonHandler extends RabbitInput {

    private final Map<String, String> groups;
    private final Map<UUID, String> cache = new ConcurrentHashMap<>();

    public PatreonHandler() {
        super("patreontiers");
        this.groups = new HashMap<>();
        for (Map.Entry<Object, CommentedConfigurationNode> entry : KiraGateway.getInstance().getConfig().node("tier_groups").childrenMap().entrySet()) {
            this.groups.put((String) entry.getKey(), entry.getValue().getString());
        }
    }

    @Override
    public void handle(JsonObject input) {
        UserManager users = LuckPermsProvider.get().getUserManager();
        for (String key : input.keySet()) {
            UUID uuid = UUID.fromString(key);

            String tier = input.get(key).getAsString();
            String group = this.groups.get(tier);

            if ((group == null ? "" : group).equals(cache.get(uuid))) {
                continue;
            }

            users.loadUser(uuid).thenAccept(user -> {
                boolean modified = false;
                for (String listedGroup : this.groups.values()) {
                    if (listedGroup.equals(group)) {
                        continue;
                    }
                    if (user.data().remove(InheritanceNode.builder(listedGroup).build()).wasSuccessful()) {
                        modified = true;
                        KiraGateway.getInstance().logger.info("Pateron: Removed {} from {} ({})", listedGroup, user.getUsername(), uuid);
                    }
                }
                if (group == null) {
                    cache.put(uuid, "");
                } else if (user.data().add(InheritanceNode.builder(group).build()).wasSuccessful()) {
                    modified = true;
                    KiraGateway.getInstance().logger.info("Patreon: Added {} from {} ({})", group, user.getUsername(), uuid);
                    cache.put(uuid, group);
                }
                if (modified) {
                    users.saveUser(user);
                }
            });
        }
    }

}
