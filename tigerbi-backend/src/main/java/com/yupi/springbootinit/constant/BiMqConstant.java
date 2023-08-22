package com.yupi.springbootinit.constant;

public interface BiMqConstant {
    /**
     * bi 交换机
     */
    String BI_EXCHANGE_name = "bi_exchange";

    /**
     * bi 队列
     */
    String BI_QUEUE_NAME = "bi_queue";

    /**
     * bi 路由键
     */
    String BI_ROUTING_KEY = "bi_routingKey";

    /**
     * bi 消息过期时间
     */
    String BI_MESSAGE_EXPIRED = "20000";

    /**
     * bi 重试交换机
     */
    String BI_RELOAD_EXCHANGE_NAME = "bi-chart-reload-exchange";

    /**
     * bi 重试路由键
     */
    String BI_RELOAD_ROUTING_KEY = "bi-chart-reload-routing";

    /**
     * bi 重试队列
     */
    String BI_RELOAD_QUEUE_NAME = "bi-chart-reload-queue";

    /**
     * bi 死信交换机
     */
    String BI_DLX_EXCHANGE_NAME = "bi-chart-dlx-exchange";

    /**
     * bi 死信队列
     */
    String BI_DLX_QUEUE_NAME = "bi-chart-dlx-queue";

    /**
     * bi 死信路由键
     */
    String BI_DLX_ROUTING_KEY = "bi-chart-dlx-routing";

}
