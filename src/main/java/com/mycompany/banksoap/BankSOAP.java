/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.banksoap;

import com.mycompany.bankjson.BankJSON;
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
import org.json.JSONObject;

/**
 *
 * @author Kasper S. Worm
 */
public class BankSOAP {
    
    public static String QUEUE_NAME;
    private final static String SEND_NAME = "Databasserne_bankSOAP";
    private final static String RECEIVE_NAME = "Databasserne_Test";
    private final static String HOST_NAME = "datdb.cphbusiness.dk";
//    private final static String HOST_NAME = "10.18.144.10";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        receive();
    }

    private static String JSONFormatter(String message) {
        JSONObject json = new JSONObject(message);

        String SSN = json.getString("SSN");
        String tempSSN = SSN.replace("-", "");
        int Months = json.getInt("Months");
        double Amount = json.getDouble("Amount");
        int Credit = json.getInt("CreditScore");

        String temp = "{ \"ssn\":\"" + tempSSN + "\","
                + " \"creditScore\":" + Credit + "\","
                + " \"loanAmount\":" + Amount + "\","
                + " \"loanDuration\": " + Months + "\" }";

        return temp;
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
        Channel SOAPChannel = connection.createChannel();
        String normalizerQueue = "Databasserne_Normalizer";

        System.out.println("\n***SENDING MESSAGE***");
        String replyKey = "soapbank";

        SOAPChannel.exchangeDeclare(SEND_NAME, "fanout");

        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties()
                .builder()
                .replyTo(normalizerQueue)
                .correlationId("BankSOAP")
                .build();

        System.out.println("Sent message: " + message);
        SOAPChannel.basicPublish(SEND_NAME, replyKey, basicProperties, message.getBytes());
        SOAPChannel.close();
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
        sendChannel.exchangeDeclare(RECEIVE_NAME, "direct");

        Channel replyChannel = connection.createChannel();

        replyChannel.queueDeclare("Databasserne_Normalizer_SOAP", true, false, false, null);
        String replyQueue = replyChannel.queueDeclare().getQueue();

        replyChannel.queueBind(replyQueue, RECEIVE_NAME, "BankSOAP");

        System.out.println("***RECEIVING MESSAGES FROM RECIP LIST***");

        // Receive the reply message
        Consumer qc = new DefaultConsumer(replyChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String receivedMessage = new String(body);
                System.out.println("Received message: " + receivedMessage);
                try {
                    send(JSONFormatter(receivedMessage));
                } catch (TimeoutException ex) {
                    Logger.getLogger(BankJSON.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        replyChannel.basicConsume(replyQueue, false, qc);
    }
    
    
}
