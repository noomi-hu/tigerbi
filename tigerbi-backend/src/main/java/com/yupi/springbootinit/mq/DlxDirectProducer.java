package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

public class DlxDirectProducer {

  private static final String WORK_EXCHANGE_NAME = "direct2_exchange";
  private static final String DEAD_EXCHANGE_NAME = "dlx_direct_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setUsername("develop");
    factory.setPassword("12345678");
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
        channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

        String queueName1 = "laoban_dlx_queue";
        channel.queueDeclare(queueName1, true, false, false, null);
        channel.queueBind(queueName1, DEAD_EXCHANGE_NAME, "laoban");

        String queueName2 = "waibao_dlx_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");

        DeliverCallback laobanDeliverCallback = (consumerTag, deliver) -> {
            String message = new String(deliver.getBody(), "UTF-8");
            channel.basicNack(deliver.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [laoban] Received '" + deliver.getEnvelope().getRoutingKey() + "':" + message + "'");
        };

        DeliverCallback waibaoDeliverCallback = (consumerTag, deliver) -> {
            String message = new String(deliver.getBody(), "UTF-8");
            channel.basicNack(deliver.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(" [waibao] Received '" + deliver.getEnvelope().getRoutingKey() + "':" + message + "'");
        };

        channel.basicConsume(queueName1, false, laobanDeliverCallback, consumerTag -> {});
        channel.basicConsume(queueName2, false, waibaoDeliverCallback, consumerTag -> {});

        Scanner scanner = new Scanner(System.in);
        while ( scanner.hasNextLine()) {
            String userInput = scanner.nextLine();
            String[] strings = userInput.split(" ");
            if (strings.length < 1) {
                continue;
            }
            String message = strings[0];
            String routingKey = strings[1];
            channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + " with routing: " + routingKey + "'");
        }
    }
  }
}