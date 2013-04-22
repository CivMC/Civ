package com.github.igotyou.FactoryMod.utility;


import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * InteractionResponse.java
 * Purpose: Object used for sending back interaction results with error/success messages
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
public class InteractionResponse 
{
	public static enum InteractionResult
	{
		SUCCESS,
		FAILURE
	}
	
	private final InteractionResult interactionResult; //The result of this interaction attempt
	private final String interactionMessage; //The message to send to player(s) after interaction attempt
	
	/**
	 * Constructor
	 */
	public InteractionResponse(InteractionResult interactionResult,
			String interactionMessage)
	{
		this.interactionResult = interactionResult;
		this.interactionMessage = interactionMessage;
	}
	
	/**
	 * Returns the interaction result message to be sent to player(s)
	 */
	public String getInteractionMessage()
	{
		return getMessageColor() + interactionMessage;
	}
	
	/**
	 * Messages the given Player this object's interaction message
	 */
	public static void messagePlayerResult(Player player, InteractionResponse interactionResponse)
	{
		player.sendMessage(interactionResponse.getInteractionMessage());
	}
	
	/**
	 * Messages the given Player all of this object's interaction message
	 */
	public static void messagePlayerResults(Player player, List<InteractionResponse> interactionResponses)
	{
		for(InteractionResponse interactionResponse:interactionResponses)
		{
			player.sendMessage(interactionResponse.getInteractionMessage());
		}
	}
	
	/**
	 * Returns the appropriate color for interaction messages based on success/failure
	 */
	private ChatColor getMessageColor()
	{
		switch (interactionResult)
		{
		case SUCCESS:
			return ChatColor.GREEN;
		case FAILURE:
			return ChatColor.RED;
		default:
			return ChatColor.YELLOW;
		}
	}
	
	/**
	 * 'interactionResult' public accessor
	 */
	public InteractionResult getInteractionResult()
	{
		return interactionResult;
	}

}