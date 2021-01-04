package vg.civcraft.mc.civmodcore.chat.dialog;

import com.google.common.base.Preconditions;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public abstract class Dialog {

	protected final Player player;
	private final Conversation conversation;

	public Dialog(final Player player) {
		this(player, CivModCorePlugin.getInstance());
	}

	public Dialog(final Player player, final String prompt) {
		this(player, CivModCorePlugin.getInstance(), prompt);
	}

	public Dialog(final Player player, final JavaPlugin plugin) {
		this(player, plugin, null);
	}

	public Dialog(final Player player, final JavaPlugin plugin, final String prompt) {
		Preconditions.checkNotNull(player, "Player cannot be null!");
		Preconditions.checkNotNull(plugin, "Plugin cannot be null!");
		this.player = player;
		Bukkit.getScheduler().runTask(plugin, (Runnable) player::closeInventory);
		this.conversation = new ConversationFactory(plugin)
				.withModality(false)
				.withLocalEcho(false)
				.withFirstPrompt(new StringPrompt() {
					@Override
					public String getPromptText(final ConversationContext context) {
						if (prompt != null) {
							return prompt;
						}
						return "";
					}
					@Override
					public Prompt acceptInput(final ConversationContext context, final String input) {
						onReply(input.split(" "));
						return Prompt.END_OF_CONVERSATION;
					}
				}).buildConversation(player);
		this.conversation.begin();
		DialogManager.registerDialog(player.getUniqueId(), this);
	}

	public abstract void onReply(String[] message);

	public abstract List<String> onTabComplete(String lastWord, String[] fullMessage);

	public Player getPlayer() {
		return this.player;
	}

	public void end() {
		this.conversation.abandon();
	}

}
