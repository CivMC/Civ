package vg.civcraft.mc.civmodcore.chatDialog;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public class LDialog extends Dialog {
	
	private Consumer<String> replyFunction;
	
	public LDialog(Player player, Consumer<String> replyFunction) {
		this(player, replyFunction, null);
	}
	
	public LDialog(Player player, Consumer<String> replyFunction, String msgToShow) {
		super(player, CivModCorePlugin.getInstance(), msgToShow);
		this.replyFunction = replyFunction;
	}

	@Override
	public void onReply(String[] message) {
		replyFunction.accept(String.join(" ", message));
	}

	@Override
	public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
		return Collections.emptyList();
	}

}
