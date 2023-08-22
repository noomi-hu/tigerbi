package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*;

public class DirectConsumer {

  private static final String EXCHANGE_NAME = "direct_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setUsername("develop");
    factory.setPassword("12345678");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.exchangeDeclare(EXCHANGE_NAME, "direct");

    String queueName1 = "xiaohu_queue";
    channel.queueDeclare(queueName1, true, false, false, null);
    channel.queueBind(queueName1, EXCHANGE_NAME, "xiaohu");

    String queueName2 = "xiaosin_queue";
    channel.queueDeclare(queueName2, true, false, false, null);
    channel.queueBind(queueName2, EXCHANGE_NAME, "xiaosin");
    System.out.println(" [*] Waiting for message. To exit press CTRL+C");

    DeliverCallback xiaohuDeliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [xiaohu] Received '" +
                delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    DeliverCallback xiaosinDeliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [xiaosin] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    channel.basicConsume(queueName1, true, xiaohuDeliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, true, xiaosinDeliverCallback, consumerTag -> { });
  }
}