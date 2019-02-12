package com.github.maxopoly.KiraBukkitGateway.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class RabbitHandler {

	private ConnectionFactory connectionFactory;
	private String incomingQueue;
	private String outgoingQueue;
	private Logger logger;
	private Connection conn;
	private Channel incomingChannel;
	private Channel outgoingChannel;
	private RabbitInputHandler inputProcessor;

	public RabbitHandler(ConnectionFactory connFac, String incomingQueue, String outgoingQueue, Logger logger) {
		this.connectionFactory = connFac;
		this.incomingQueue = incomingQueue;
		this.outgoingQueue = outgoingQueue;
		this.logger = logger;
		inputProcessor = new RabbitInputHandler(logger);
	}

	public boolean setup() {
		try {
			conn = connectionFactory.newConnection();
			incomingChannel = conn.createChannel();
			outgoingChannel = conn.createChannel();
			incomingChannel.queueDeclare(incomingQueue, false, false, false, null);
			outgoingChannel.queueDeclare(outgoingQueue, false, false, false, null);
			return true;
		} catch (IOException | TimeoutException e) {
			logger.severe("Failed to setup rabbit connection: " + e.toString());
			return false;
		}
	}

	public void beginAsyncListen() {
		new BukkitRunnable() {

			@Override
			public void run() {
				DeliverCallback deliverCallback = (consumerTag, delivery) -> {
					try {
						String message = new String(delivery.getBody(), "UTF-8");
						System.out.println(" [x] Received '" + message + "'");
						inputProcessor.handle(message);
					} catch (Exception e) {
						logger.severe("Exception in rabbit handling: " + e.toString());
						e.printStackTrace();
					}
				};
				try {
					incomingChannel.basicConsume(incomingQueue, true, deliverCallback, consumerTag -> {
					});
				} catch (IOException e) {
					logger.severe("Error in rabbit listener: " + e.toString());
				}
			}
		}.runTask(KiraBukkitGatewayPlugin.getInstance());
	}

	public void shutdown() {
		try {
			incomingChannel.close();
			outgoingChannel.close();
			conn.close();
		} catch (IOException | TimeoutException e) {
			logger.severe("Failed to close rabbit connection: " + e);
		}
	}

	public boolean sendMessage(String msg) {
		try {
			outgoingChannel.basicPublish("", outgoingQueue, null, msg.getBytes("UTF-8"));
			return true;
		} catch (IOException e) {
			logger.severe("Failed to send rabbit message: " + e);
			return false;
		}
	}
}
