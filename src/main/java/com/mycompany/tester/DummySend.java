/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tester;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Kasper S. Worm
 */
public class DummySend {

    private final static String TRANSLATOR_NAME = "Databasserne_Test";
//    private final static String HOST_NAME = "10.18.144.10";/
//    private final static String HOST_NAME = "datdb.cphbusiness.dk";
    private final static String HOST_NAME = "5.179.80.218";
    private static final String MESSAGE = "{\"SSN\":160578-9787,\"CreditScore\":598,\"Amount\":10.0,\"Months\":360}";

    public static void main(String[] args) throws IOException, TimeoutException {
        DummySend sender = new DummySend();
        sender.send();

    }

    private void send() throws IOException, TimeoutException {
        List<String> bankList = new ArrayList();
        bankList.add("BankXML");
        bankList.add("BankJSON");
//        bankList.add("BankSOAP");
//        bankList.add("BankNODE");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        System.out.println("\n***SENDING MESSAGE***");

        channel.exchangeDeclare(TRANSLATOR_NAME, "direct");

        for (String bank : bankList) {
            System.out.println("Sent message: " + MESSAGE);
            channel.basicPublish(TRANSLATOR_NAME, bank, null, MESSAGE.getBytes());
            System.out.println("DONE\n");
        }

        channel.close();
        connection.close();
    }

}
