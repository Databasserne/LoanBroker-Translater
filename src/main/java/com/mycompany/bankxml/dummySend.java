/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bankxml;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Kasper S. Worm
 */
public class dummySend {

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

        send(connection, replyChannel, replyQueue);
    }
    
    private static void send(Connection conn, Channel chan, String queue) throws IOException, TimeoutException {
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
}
