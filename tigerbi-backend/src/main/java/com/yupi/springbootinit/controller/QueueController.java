package com.yupi.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 队列测试
 *
 * @author 叶虎强
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev", "local"})
public class QueueController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    //接收一个参数name，然后将任务添加到线程池中
    public void add(String name) {
        //使用CompletableFuture运行一个异步任务
        CompletableFuture.runAsync(()->{
            //打印一条日志信息
            log.info("任务执行中：" + name + "，执行人" + Thread.currentThread().getName());
            try {
                //让线程睡眠10min，模拟长时间运行任务
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                e.printStackTrace();;
            }
        //异步任务在threadPoolExecutor中执行
        }, threadPoolExecutor);
    }

    @GetMapping("/get")
    //接收一个参数name，然后将任务添加到线程池中
    public String  get() {
        Map<String , Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度", size);
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数", taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成任务数", completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作的线程数", activeCount);
        return JSONUtil.toJsonStr(map);
    }
}
