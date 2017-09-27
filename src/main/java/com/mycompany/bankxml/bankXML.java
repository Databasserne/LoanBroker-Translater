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
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author jonassimonsen
 */
public class bankXML {

    private final static String QUEUE_NAME = "cphbusiness.bankXML";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

        ConnectionFactory connfac = new ConnectionFactory();
        connfac.setHost("datdb.cphbusiness.dk");
        //connfac.setPort(15672);
        connfac.setUsername("student");
        connfac.setPassword("cph");

        Connection connection = null;

        try {
            connection = connfac.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(QUEUE_NAME, "fanout");
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, QUEUE_NAME, "");
            channel.basicQos(1);

            System.out.println(connection);
            System.out.println("waiting...");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, false, consumer);

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());

                System.out.println("Received '" + message + "'");
                String reply = "testing...";
                System.out.println("Done");

                AMQP.BasicProperties bp = delivery.getProperties();
                //Unpack the reply queue
                String replyQueue = bp.getReplyTo();

                System.out.print("\tReply Message: " + reply);
                System.out.print("\t. Replying to: " + replyQueue + "\n");

                channel.basicPublish("", replyQueue, bp, reply.getBytes());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException _ignore) {
                }
            }
        }
    }
}
