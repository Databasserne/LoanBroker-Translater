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
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jonassimonsen & Kasper S. Worm
 */
public class bankXML implements Runnable {

    public static String QUEUE_NAME;
    private final static String RECEIVING_QUEUE_NAME = "cphbusiness.bankXML";
    private final static String HOST_NAME = "10.18.144.10"; //datdb.cphbusiness.dk

    private static Connection conn;
    private static Channel chan;
    private String qu;

    public bankXML(Connection connection, Channel channel, String queue) {
        this.conn = connection;
        this.chan = channel;
        this.qu = queue;
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(RECEIVING_QUEUE_NAME, "fanout");
        String test = channel.queueDeclare().getQueue();
        QUEUE_NAME = test;

        Thread t = new Thread(new bankXML(connection, channel, test));
        t.start();
        //TODO - receive message from other source and call send wih that message
        send(test);
    }

    private static void send(String queue) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(RECEIVING_QUEUE_NAME, "fanout");
        AMQP.BasicProperties.Builder propertiesBuilder = new AMQP.BasicProperties.Builder();
        propertiesBuilder.replyTo(queue);
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

    /**
     * Listens to response from the bank
     *
     * @throws ShutdownSignalException
     * @throws InterruptedException
     * @throws ConsumerCancelledException
     */
    private static void receive(String queue, Connection connection, Channel channel) throws ShutdownSignalException, InterruptedException, ConsumerCancelledException, TimeoutException, IOException {
        try {
            channel.basicQos(1);

            System.out.println("***DEBUG - Connection: " + connection);
            System.out.println("waiting for response...");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, true, consumer);

            //Keep listening to new messages from the bank
            while (true) {

                System.out.println("connection: " + connection);
                System.out.println("channel   : " + channel);
                System.out.println("while start");
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                String message = new String(delivery.getBody());

                System.out.println("Received '" + message + "'");
                System.out.println("Done");

                AMQP.BasicProperties bp = delivery.getProperties();

                String reply = "testing...";
                String replyQueue = bp.getReplyTo();

                System.out.print("\tReply Message: " + reply);
                System.out.print("\t. Replying to: " + replyQueue + "\n");

                System.out.println("delivery: " + delivery);
                channel.basicPublish("", replyQueue, bp, reply.getBytes());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                System.out.println("done done");
                //TODO - Send respons to normalizer
            }

        } catch (ConsumerCancelledException | ShutdownSignalException | IOException | InterruptedException e) {
            System.out.println("shit hit the fan");
            System.out.println(e.getMessage());
        } finally {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public void run() {
        System.out.println("test");
        try {
            receive(qu, conn, chan);
        } catch (IOException | TimeoutException | ShutdownSignalException | InterruptedException | ConsumerCancelledException ex) {
            Logger.getLogger(bankXML.class.getName()).log(Level.SEVERE, "Thread-run-catch", ex);
        }
    }
}
