package com.github.maxopoly.KiraBukkitGateway.impersonation;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PseudoConsoleSender implements ConsoleCommandSender {

	private ConsoleCommandSender actualSender;
	private List<String> replies;
	private UUID actualUser;
	private long discordChannelId;

	public PseudoConsoleSender(UUID actualUser, ConsoleCommandSender actualSender, long discordChannelId) {
		this.actualSender = actualSender;
		replies = new LinkedList<>();
		this.actualUser = actualUser;
		this.discordChannelId = discordChannelId;
	}
	
	public synchronized List<String> getRepliesAndFinish() {
		List<String> result = replies;
		replies = null;
		return result;
	}
	
	private synchronized void handleReply(String input) {
		if (replies != null) {
			replies.add(input);
			return;
		}
		KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(actualUser, input, discordChannelId);
	}

	@Override
	public String getName() {
		return actualSender.getName();
	}

	@Override
	public Server getServer() {
		return actualSender.getServer();
	}

	@Override
	public void sendMessage(String arg0) {
		handleReply(arg0);
	}

	@Override
	public void sendMessage(String[] arg0) {
		for (String s : arg0) {
			handleReply(s);
		}
	}

	@Override
	public void sendMessage(@Nullable final UUID uuid, @Nonnull final String message) {
		this.actualSender.sendMessage(uuid, message);
	}

	@Override
	public void sendMessage(@Nullable final UUID uuid, @Nonnull final String[] messages) {
		this.actualSender.sendMessage(uuid, messages);
	}

	@Override
	public Spigot spigot() {
		return actualSender.spigot();
	}

	@Override
	public @NotNull Component name() {
		return this.actualSender.name();
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {
		return actualSender.addAttachment(arg0);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		return actualSender.addAttachment(arg0, arg1);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
		return actualSender.addAttachment(arg0, arg1, arg2);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
		return actualSender.addAttachment(arg0, arg1, arg2, arg3);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return actualSender.getEffectivePermissions();
	}

	@Override
	public boolean hasPermission(String arg0) {
		return actualSender.hasPermission(arg0);
	}

	@Override
	public boolean hasPermission(Permission arg0) {
		return actualSender.hasPermission(arg0);
	}

	@Override
	public boolean isPermissionSet(String arg0) {
		return actualSender.isPermissionSet(arg0);
	}

	@Override
	public boolean isPermissionSet(Permission arg0) {
		return actualSender.isPermissionSet(arg0);
	}

	@Override
	public void recalculatePermissions() {
		actualSender.recalculatePermissions();
	}

	@Override
	public void removeAttachment(PermissionAttachment arg0) {
		actualSender.removeAttachment(arg0);
	}

	@Override
	public boolean isOp() {
		return actualSender.isOp();
	}

	@Override
	public void setOp(boolean arg0) {
		actualSender.setOp(arg0);
	}

	@Override
	public void abandonConversation(Conversation arg0) {
		actualSender.abandonConversation(arg0);
	}

	@Override
	public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
		actualSender.abandonConversation(arg0, arg1);
	}

	@Override
	public void acceptConversationInput(String arg0) {
		actualSender.acceptConversationInput(arg0);

	}

	@Override
	public boolean beginConversation(Conversation arg0) {
		return actualSender.beginConversation(arg0);
	}

	@Override
	public boolean isConversing() {
		return actualSender.isConversing();
	}

	@Override
	public void sendRawMessage(String arg0) {
		handleReply(arg0);
	}

	@Override
	public void sendRawMessage(@Nullable final UUID uuid, @Nonnull final String s) {

	}

}
