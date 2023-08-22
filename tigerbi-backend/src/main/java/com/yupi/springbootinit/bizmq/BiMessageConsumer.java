package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.BiMqConstant;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

//使用@Component注解标记该类为一个组件，让Spring框架能够扫描并将其纳入管理
@Component
//使用@Slf4j注解生成日志记录器
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

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
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    //@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag是一个方法参数注解，用于从消息头中获取投递标签（deliveryTag）
    //在RabbitMQ中，每条消息都会被分配一个唯一的投递标签，用于标识该消息在通道中的投递状态和顺序，
    //通过@Header(AmqpHeaders.DELIVERY_TAG)注解可以从消息头中取出该投递标签，并将其赋值给long deliveryTag参数
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }
        //先修改图表任务状态为“执行中”，等执行成功后，修改为“已完成”，保存执行结果；执行失败后，状态修改为“失败”，记录任务失败信息。（为了防止同一个任务被多次执行）
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        //把任务状态改为执行中
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        //如果提交失败（一般情况下，更新失败可能意味着数据库出问题了）
        if (!b) {
            handlerChartUpdateError(chart.getId(), "更新图表执行中状态失败");
            return;
        }

        //调用AI
        //拿到返回结果
        String result = aiManager.doChat(CommonConstant.BI_MODEL_ID, buildUserInput(chart));
        //对返回结果做拆分，按照5个中括号进行拆分
        String[] splits = result.split("【【【【【");
        //拆分后校验
        if (splits.length < 3) {
            channel.basicNack(deliveryTag, false,false);
            handlerChartUpdateError(chart.getId(), "AI生成错误");
            return;
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        //调用AI得到结果之后再更新一次
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus("succeed");
        boolean updataResult = chartService.updateById(updateChartResult);
        if (!updataResult) {
            channel.basicNack(deliveryTag, false,false);
            handlerChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
        //投递标签时一个数字标识，它在消息消费者接收到消息后用于向RabbitMQ确认消息的处理状态。
        //通过将投递标签传递给channel.basicAck(deliveryTag, false)方法，可以告知RabbitMQ该消息已经成功处理，可以进行确认和从队列中删除
        channel.basicAck(deliveryTag, false);
    }

    private String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;
        //如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            //就将分析目标拼接上“请使用”+图表类型
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }

    private void handlerChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage("execMessage");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "，" + execMessage);
        }
    }
}
