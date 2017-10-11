/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.normalizer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author jonassimonsen
 */
public class Normalizer {

    public static String QUEUE_NAME;
    private final static String EXCHANGE_NAME = "cphbusiness.bankXML";
    private final static String HOST_NAME = "10.18.144.10"; /*"datdb.cphbusiness.dk";

    /**
     * Listens to response from the bank
     *
     * @throws ShutdownSignalException
     * @throws InterruptedException
     * @throws ConsumerCancelledException
     */
    public static void main(String[] args) throws IOException, TimeoutException, ShutdownSignalException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();

        Channel replyChannel = connection.createChannel();
        String replyQueue = "Databasserne_Normalizer_XML";
        
        receive(connection, replyChannel, replyQueue);

    }

    private static void receive(Connection conn, Channel chan, String queue) throws ShutdownSignalException, InterruptedException, ConsumerCancelledException, TimeoutException, IOException {
        System.out.println("***RECEIVING MESSAGES FROM BANK***");

        // Receive the reply message
        Consumer qc = new DefaultConsumer(chan) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String receivedMessage = new String(body);
                System.out.println(properties.getCorrelationId());
                System.out.println("\nReceived: " + receivedMessage);
                chan.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        chan.basicConsume(queue, false, qc);
        
    }

}
