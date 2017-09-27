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

    private final static String RECEIVING_QUEUE_NAME = "cphbusiness.bankXML";
    private final static String SENDING_QUEUE = "translator.group1.cphbusiness.bankXML";
    private final static String HOST_NAME = "10.18.144.10"; //datdb.cphbusiness.dk

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(RECEIVING_QUEUE_NAME, "fanout");

        AMQP.BasicProperties.Builder propertiesBuilder = new AMQP.BasicProperties.Builder();
        propertiesBuilder.replyTo(RECEIVING_QUEUE_NAME);
        AMQP.BasicProperties properties = propertiesBuilder.build();

        String message = "<LoanRequest><ssn>12345678</ssn>"
                + "<creditScore>685</creditScore>"
                + "<loanAmount>1000.0</loanAmount><loanDuration>1973-01-01 01:00:00.0 CET</loanDuration>"
                + "</LoanRequest>";

        System.out.println("Sent message: " + message);
        channel.basicPublish(RECEIVING_QUEUE_NAME, "", properties, message.getBytes());

        channel.close();
        connection.close();
    }
}
