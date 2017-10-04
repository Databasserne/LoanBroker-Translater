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
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Kasper S. Worm
 */
public class dummySend {

    private final static String RECEIVING_QUEUE_NAME = "cphbusiness.bankXML";
    private final static String HOST_NAME = "10.18.144.10"; //datdb.cphbusiness.dk
    public static String QUEUE_NAME;
    private String replyQueue;
    private Channel channel;
    private Connection connection;
//    private final static String HOST_NAME = "10.18.144.10"; 

//    public static void main(String[] args) throws IOException, TimeoutException {
    public dummySend() throws IOException, TimeoutException {
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(HOST_NAME);
//        connection = factory.newConnection();
//        channel = connection.createChannel();
//
//        replyQueue = channel.queueDeclare().getQueue();
////        channel.exchangeDeclare(RECEIVING_QUEUE_NAME, "fanout");

//        channel.close();
//        connection.close();
    }

//    public String call(String message) throws IOException, InterruptedException {
//        String corrID = UUID.randomUUID().toString();
//
//        Map<String, Object> headers = new HashMap();
//        headers.put("HEADER", "group1");
//
//        AMQP.BasicProperties.Builder propertiesBuilder = new AMQP.BasicProperties.Builder();
//        propertiesBuilder.replyTo(RECEIVING_QUEUE_NAME);
//        propertiesBuilder.headers(headers);
//        propertiesBuilder.correlationId(corrID);
//        AMQP.BasicProperties properties = propertiesBuilder.build();
//
//        
//
////        channel.basicPublish(RECEIVING_QUEUE_NAME, "", properties, response.getBytes());
//        channel.basicPublish("", RECEIVING_QUEUE_NAME, properties, message.getBytes());
//        System.out.println("Sent message: " + message);
//
//        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
//
//        channel.basicConsume(replyQueue, true, new DefaultConsumer(channel) {
//            @Override
//            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                if (properties.getCorrelationId().equals(corrID)) {
//                    response.offer(new String(body, "UTF-8"));
//                }
//            }
//        });
//
//        return response.take();
//    }
//
//    public void close() throws IOException, TimeoutException {
//        connection.close();
//        channel.close();
//    }
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
//        dummySend sendMsg = new dummySend();
//
//        String returnMSG = "<LoanRequest><ssn>12345678</ssn>"
//                + "<creditScore>685</creditScore>"
//                + "<loanAmount>1000.0</loanAmount><loanDuration>1973-01-01 01:00:00.0 CET</loanDuration>"
//                + "</LoanRequest>";
//        
//        
//        System.out.println("Requising send message");
//        String response = sendMsg.call(returnMSG);
//        System.out.println("Got: " + response);
//
//        sendMsg.close();
        dummySend send = new dummySend();
        System.out.println(bankXML.QUEUE_NAME);
        send.send(bankXML.QUEUE_NAME);

    }

    private static void send(String queue) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String test = channel.queueDeclare().getQueue();
        
        channel.exchangeDeclare(RECEIVING_QUEUE_NAME, "fanout");
        AMQP.BasicProperties.Builder propertiesBuilder = new AMQP.BasicProperties.Builder();
        propertiesBuilder.replyTo(test);
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
