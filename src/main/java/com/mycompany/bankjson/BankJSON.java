/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bankjson;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jonassimonsen
 */
public class BankJSON {
    public static String QUEUE_NAME;
    private final static String EXCHANGE_NAME = "cphbusiness.bankJSON";
    private final static String HOST_NAME = /*"10.18.144.10";*/ "datdb.cphbusiness.dk";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(HOST_NAME);
//        factory.setUsername("student");
//        factory.setPassword("cph");
//        Connection connection = factory.newConnection();
//
//        Channel sendChannel = connection.createChannel();
//        sendChannel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//
//        Channel replyChannel = connection.createChannel();
//        String replyQueue = replyChannel.queueDeclare().getQueue();

        //TODO - receive message from other source and call send wih that message
        receive();
        //send(replyChannel, replyQueue);
    }

    /**
     * Send a message
     *
     * @param chan channel to send message to
     * @param queue replyqueue
     * @throws IOException
     * @throws TimeoutException
     */
    private static void send(String message) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel normalizerChannel = connection.createChannel();
        String normalizerQueue = normalizerChannel.queueDeclare().getQueue();
        
        System.out.println("***SENDING MESSAGE***");
        String replyKey = "jsonbank";

        normalizerChannel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties()
                .builder()
                .replyTo(normalizerQueue)
                .build();
        
//        String message = "{\"ssn\":1605789787, \"creditScore\":598, \"loanAmount\":10.0, \"loanDuration\":360}";

        System.out.println("Sent message: " + message);
        normalizerChannel.basicPublish(EXCHANGE_NAME, replyKey, basicProperties, message.getBytes());
        normalizerChannel.close();
        connection.close();
    }

    /**
     * Listens to response from the bank
     *
     * @throws ShutdownSignalException
     * @throws InterruptedException
     * @throws ConsumerCancelledException
     */
    private static void receive() throws ShutdownSignalException, InterruptedException, ConsumerCancelledException, TimeoutException, IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();

        Channel sendChannel = connection.createChannel();
        sendChannel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        Channel replyChannel = connection.createChannel();
        String replyQueue = replyChannel.queueDeclare().getQueue();
        
        System.out.println("***DEBUG - Connection: " + connection);
        System.out.println("***RECEIVING MESSAGES FROM BANK***");

        // Receive the reply message
        Consumer qc = new DefaultConsumer(replyChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String replyMessage = new String(body);
                System.out.println("Reply: " + replyMessage);
                try {
                    send(replyMessage);
                } catch (TimeoutException ex) {
                    Logger.getLogger(BankJSON.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        replyChannel.basicConsume(replyQueue, false, qc);
    }
}