package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxDirectConsumer {

  private static final String WORK_EXCHANGE_NAME = "direct2_exchange";
  private static final String DEAD_EXCHANGE_NAME = "dlx_direct_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setUsername("develop");
    factory.setPassword("12345678");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

    Map<String, Object> args1 = new HashMap<>();
    args1.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
    args1.put("x-dead-letter-routing-key", "waibao");

    String queueName1 = "xiaodog_queue";
    channel.queueDeclare(queueName1, true, false, false, args1);
    channel.queueBind(queueName1, WORK_EXCHANGE_NAME, "xiaodog");

    Map<String, Object> args2 = new HashMap<>();
    args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
    args2.put("x-dead-letter-routing-key", "laoban");

    String queueName2 = "xiaocat_queue";
    channel.queueDeclare(queueName2, true, false, false, args2);
    channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "xiaocat");

    System.out.println(" [*] Waiting for message. To exit press CTRL+C");

    DeliverCallback xiaodogDeliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        System.out.println(" [xiaodog] Received '" +
                delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    DeliverCallback xiaocatDeliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
      System.out.println(" [xiaocat] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    channel.basicConsume(queueName1, false, xiaodogDeliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, false, xiaocatDeliverCallback, consumerTag -> { });
  }
}