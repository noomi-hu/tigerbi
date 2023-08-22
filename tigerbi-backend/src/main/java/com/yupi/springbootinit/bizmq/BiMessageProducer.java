package com.yupi.springbootinit.bizmq;

import com.yupi.springbootinit.constant.BiMqConstant;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息的方法
     *
     * @param message
     */
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_name, BiMqConstant.BI_ROUTING_KEY, message,
                message1 -> {
                    message1.getMessageProperties().setExpiration(BiMqConstant.BI_MESSAGE_EXPIRED);
                    message1.getMessageProperties().setDeliveryMode(MessageDeliveryMode.fromInt(2));
                    return message1;
                });
    }
}
