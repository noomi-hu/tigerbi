package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class TopicConsumer {

  private static final String EXCHANGE_NAME = "topic_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setUsername("develop");
    factory.setPassword("12345678");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.exchangeDeclare(EXCHANGE_NAME, "topic");

    String queueName1 = "frontend_queue";
    channel.queueDeclare(queueName1, true, false, false, null);
    channel.queueBind(queueName1, EXCHANGE_NAME, "#.前端.#");

    String queueName2 = "backend_queue";
    channel.queueDeclare(queueName2, true, false, false, null);
    channel.queueBind(queueName2, EXCHANGE_NAME, "#.后端.#");

    String queueName3 = "product_queue";
    channel.queueDeclare(queueName3, true, false, false, null);
    channel.queueBind(queueName3, EXCHANGE_NAME, "#.产品.#");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback frontendDeliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [frontend] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    DeliverCallback backendDeliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [backend] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    DeliverCallback productDeliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [product] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    channel.basicConsume(queueName1, true, frontendDeliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, true, backendDeliverCallback, consumerTag -> { });
    channel.basicConsume(queueName3, true, productDeliverCallback, consumerTag -> { });
  }
}