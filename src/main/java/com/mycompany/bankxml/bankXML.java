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

/**
 *
 * @author jonassimonsen & Kasper S. Worm
 */
public class bankXML {

    private final static String RECEIVING_QUEUE_NAME = "cphbusiness.bankXML";
    private final static String HOST_NAME = "10.18.144.10"; //datdb.cphbusiness.dk

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        receive();
    }

    /**
     * Listens to response from the bank
     *
     * @throws ShutdownSignalException
     * @throws InterruptedException
     * @throws ConsumerCancelledException
     */
    private static void receive() throws ShutdownSignalException, InterruptedException, ConsumerCancelledException, TimeoutException {
        ConnectionFactory connfac = new ConnectionFactory();
        connfac.setHost(HOST_NAME);
        //connfac.setPort(15672);
        connfac.setUsername("student");
        connfac.setPassword("cph");

        Connection connection = null;
        Channel channel = null;

        try {
            //Connect
            connection = connfac.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(RECEIVING_QUEUE_NAME, "fanout");
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, RECEIVING_QUEUE_NAME, "");
            channel.basicQos(1);

            System.out.println("***DEBUG - Connection: " + connection);
            System.out.println("waiting for response...");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, false, consumer);

            //Keep listening to new messages from the bank
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());

                System.out.println("Received '" + message + "'");
                String reply = "testing...";
                System.out.println("Done");

                AMQP.BasicProperties bp = delivery.getProperties();

                String replyQueue = bp.getReplyTo();

                System.out.print("\tReply Message: " + reply);
                System.out.print("\t. Replying to: " + replyQueue + "\n");

                channel.basicPublish("", replyQueue, bp, reply.getBytes());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                //TODO - Send respons to normalizer
            }

            //Catch exception and close connection/channel
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException connectionCloseException) {
                }
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (TimeoutException | IOException channelCloseException) {
                }
            }
        }
    }
}
