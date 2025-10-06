package net.civmc.kiragatewayvelocity.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;

public class RabbitHandler {

    private ConnectionFactory connectionFactory;
    private String incomingQueue;
    private String outgoingQueue;
    private Logger logger;
    private Connection conn;
    private Channel incomingChannel;
    private Channel outgoingChannel;

    public RabbitHandler(ConnectionFactory connFac, String incomingQueue, String outgoingQueue, Logger logger) {
        this.connectionFactory = connFac;
        this.incomingQueue = incomingQueue;
        this.outgoingQueue = outgoingQueue;
        this.logger = logger;
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
            logger.error("Failed to setup rabbit connection", e);
            return false;
        }
    }

    public void shutdown() {
        try {
            incomingChannel.close();
            outgoingChannel.close();
            conn.close();
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to close rabbit connection", e);
        }
    }

    public boolean sendMessage(String msg) {
        try {
            outgoingChannel.basicPublish("", outgoingQueue, null, msg.getBytes("UTF-8"));
            return true;
        } catch (IOException e) {
            logger.error("Failed to send rabbit message", e);
            return false;
        }
    }
}
