/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bankxml;

import com.rabbitmq.client.AMQP;
import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author jonassimonsen & Kasper S. Worm
 */
public class bankXML {

    public static String QUEUE_NAME;
    private final static String EXCHANGE_NAME = "cphbusiness.bankXML";
    private final static String HOST_NAME = "10.18.144.10"; //datdb.cphbusiness.dk

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();

        Channel sendChannel = connection.createChannel();
        sendChannel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        Channel replyChannel = connection.createChannel();
        String replyQueue = replyChannel.queueDeclare().getQueue();

        //TODO - receive message from other source and call send wih that message
        receive(connection, replyChannel, replyQueue);
        send(replyChannel, replyQueue);
    }

    /**
     * Send a message
     *
     * @param chan channel to send message to
     * @param queue replyqueue
     * @throws IOException
     * @throws TimeoutException
     */
    private static void send(Channel chan, String queue) throws IOException, TimeoutException {
        System.out.println("***SENDING MESSAGE***");
        String replyKey = "xmlbank";

        chan.exchangeDeclare(EXCHANGE_NAME, "fanout");

        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties()
                .builder()
                .contentType("text/plain")
                .deliveryMode(1)
                .replyTo(queue)
                .build();

        String message = "<LoanRequest>"
                + "<ssn>12345678</ssn>"
                + "<creditScore>685</creditScore>"
                + "<loanAmount>1000.0</loanAmount>"
                + "<loanDuration>1973-01-01 01:00:00.0 CET</loanDuration>"
                + "</LoanRequest>";

        System.out.println("Sent message: " + message);
        chan.basicPublish(EXCHANGE_NAME, replyKey, basicProperties, message.getBytes());

    }

    /**
     * Listens to response from the bank
     *
     * @throws ShutdownSignalException
     * @throws InterruptedException
     * @throws ConsumerCancelledException
     */
    private static void receive(Connection conn, Channel chan, String queue) throws ShutdownSignalException, InterruptedException, ConsumerCancelledException, TimeoutException, IOException {
        System.out.println("***DEBUG - Connection: " + conn);
        System.out.println("***RECEIVING MESSAGES FROM BANK***");

        // Receive the reply message
        Consumer qc = new DefaultConsumer(chan) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String replyMessage = new String(body);
                System.out.println("Reply: " + replyMessage);
            }
        };
        chan.basicConsume(queue, false, qc);

    }

}
