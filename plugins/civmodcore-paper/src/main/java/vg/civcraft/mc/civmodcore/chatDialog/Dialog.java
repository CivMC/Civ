package vg.civcraft.mc.civmodcore.chatDialog;

import java.util.List;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Dialog {

	protected Player player;

	private Conversation convo;

	public Dialog(Player player, JavaPlugin plugin) {
		this(player, plugin, null);
	}

	public Dialog(Player player, JavaPlugin plugin, final String toDisplay) {
		DialogManager.instance.registerDialog(player, this);
		this.player = player;

		convo = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
				.withFirstPrompt(new StringPrompt() {

					@Override
					public String getPromptText(ConversationContext arg0) {
						if (toDisplay != null) {
							return toDisplay;
						}
						return "";
					}

					@Override
					public Prompt acceptInput(ConversationContext arg0, String arg1) {
						onReply(arg1.split(" "));
						return Prompt.END_OF_CONVERSATION;
					}

				}).buildConversation(player);

		convo.begin();
	}

	public abstract void onReply(String[] message);

	public abstract List<String> onTabComplete(String wordCompleted, String[] fullMessage);

	public Player getPlayer() {
		return player;
	}

	public void end() {
		convo.abandon();
	}

}
