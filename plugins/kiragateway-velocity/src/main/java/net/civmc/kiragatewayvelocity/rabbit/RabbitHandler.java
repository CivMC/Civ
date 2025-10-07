package net.civmc.kiragatewayvelocity.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.DeliverCallback;
import net.civmc.kiragatewayvelocity.KiraGateway;
import org.slf4j.Logger;

public class RabbitHandler {

    private ConnectionFactory connectionFactory;
    private String incomingQueue;
    private String outgoingQueue;
    private Logger logger;
    private Connection conn;
    private Channel incomingChannel;
    private Channel outgoingChannel;
    private final RabbitInputHandler inputProcessor;

    public RabbitHandler(ConnectionFactory connFac, String incomingQueue, String outgoingQueue, Logger logger) {
        this.connectionFactory = connFac;
        this.incomingQueue = incomingQueue;
        this.outgoingQueue = outgoingQueue;
        this.logger = logger;
        this.inputProcessor = new RabbitInputHandler(logger);
    }

    public boolean setup() {
        try {
            conn = connectionFactory.newConnection();
            incomingChannel = conn.createChannel();
            outgoingChannel = conn.createChannel();
            incomingChannel.exchangeDeclare(incomingQueue, "direct", false, false, null);
            outgoingChannel.queueDeclare(outgoingQueue, false, false, false, null);
            return true;
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to setup rabbit connection", e);
            return false;
        }
    }

    public void beginAsyncListen() {
        KiraGateway.getInstance().getProxy().getScheduler().buildTask(KiraGateway.getInstance(), () -> {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    logger.info(" [x] Received '" + message + "'");
                    inputProcessor.handle(message);
                } catch (Exception e) {
                    logger.error("Exception in rabbit handling", e);
                }
            };
            try {
                logger.info("Starting listen");
                String queue = incomingChannel.queueDeclare().getQueue();
                incomingChannel.queueBind(queue, incomingQueue, KiraGateway.PROXY_SERVER_NAME);
                incomingChannel.basicConsume(queue, true, deliverCallback, consumerTag -> {
                });
            } catch (IOException e) {
                logger.error("Error in rabbit listener", e);
            }
        }).schedule();
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
