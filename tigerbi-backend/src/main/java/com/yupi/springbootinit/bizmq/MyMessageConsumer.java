package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Header;

//使用@Component注解标记该类为一个组件，让Spring框架能够扫描并将其纳入管理
@Component
//使用@Slf4j注解生成日志记录器
@Slf4j
public class MyMessageConsumer {
    /**
     * 接收消息的方法
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    //使用@SneakyThrows注解简化异常处理
    @SneakyThrows
    //使用注解指定要监听的队列名称为“code_queue”，并设置消息的确认机制为手动确认
    @RabbitListener(queues = {"code_queue"}, ackMode = "MANUAL")
    //@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag是一个方法参数注解，用于从消息头中获取投递标签（deliveryTag）
    //在RabbitMQ中，每条消息都会被分配一个唯一的投递标签，用于标识该消息在通道中的投递状态和顺序，
    //通过@Header(AmqpHeaders.DELIVERY_TAG)注解可以从消息头中取出该投递标签，并将其赋值给long deliveryTag参数
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        //投递标签时一个数字标识，它在消息消费者接收到消息后用于向RabbitMQ确认消息的处理状态。
        //通过将投递标签传递给channel.basicAck(deliveryTag, false)方法，可以告知RabbitMQ该消息已经成功处理，可以进行确认和从队列中删除
        channel.basicAck(deliveryTag, false);
    }
}
