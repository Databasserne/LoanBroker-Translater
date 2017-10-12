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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author jonassimonsen & Kasper S. Worm
 */
public class BankXML {

    public static String QUEUE_NAME;
    private final static String SEND_NAME = "cphbusiness.bankXML";
    private final static String RECEIVE_NAME = "Databasserne_Test";
    private final static String HOST_NAME = "5.179.80.218";
//    private final static String HOST_NAME = "datdb.cphbusiness.dk";
//    private final static String HOST_NAME = "10.18.144.10"; 

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        receive();
    }

    private static String jsonToXmlConverter(String message) {
        JSONObject json = new JSONObject(message);

        String SSN = json.getString("SSN");
        String tempSSN = SSN.replace("-", "");
        int Months = json.getInt("Months");
        double Amount = json.getDouble("Amount");
        int Credit = json.getInt("CreditScore");

        Calendar cal = Calendar.getInstance();
        cal.set(1970, 0, 1);
        cal.add(Calendar.MONTH, Months);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

        String tempDate = format.format(cal.getTime());

        String temp = "<LoanRequest>\n"
                + "   <ssn>" + tempSSN + "</ssn>\n"
                + "   <creditScore>" + Credit + "</creditScore>\n"
                + "   <loanAmount>" + Amount + "</loanAmount>\n"
                + "   <loanDuration>" + tempDate + "</loanDuration>\n"
                + "</LoanRequest>";

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
        Channel XMLChannel = connection.createChannel();
        String normalizerQueue = "Databasserne_Normalizer";

        System.out.println("\n***SENDING MESSAGE***");
        String replyKey = "xmlbank";

        XMLChannel.exchangeDeclare(SEND_NAME, "fanout");

        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties()
                .builder()
                .replyTo(normalizerQueue)
                .correlationId("BankXML")
                .build();

        System.out.println("Sent message: " + message);
        XMLChannel.basicPublish(SEND_NAME, replyKey, basicProperties, message.getBytes());
        XMLChannel.close();
        connection.close();
    }

    private static void receive() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();

        Channel sendChannel = connection.createChannel();
        sendChannel.exchangeDeclare(RECEIVE_NAME, "direct");

        Channel replyChannel = connection.createChannel();
        String replyQueue = replyChannel.queueDeclare().getQueue();

        replyChannel.queueBind(replyQueue, RECEIVE_NAME, "BankXML");

        System.out.println("***RECEIVING MESSAGES FROM RECIP LIST***");

        // Receive the reply message
        Consumer qc = new DefaultConsumer(replyChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String receivedMessage = new String(body);
                System.out.println("Received message: " + receivedMessage);
                try {
                    send(jsonToXmlConverter(receivedMessage));
                } catch (TimeoutException ex) {
                    Logger.getLogger(BankXML.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        replyChannel.basicConsume(replyQueue, false, qc);
    }
}
