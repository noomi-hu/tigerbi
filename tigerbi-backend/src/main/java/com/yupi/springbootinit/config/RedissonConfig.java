package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//从application.yml中读取前缀为“spring.redis”的配置项
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    private Integer database;

    private String host;

    private Integer port;

    @Bean
    public RedissonClient redissonClient() {
        //创建配置对象
        Config config = new Config();
        //添加单机Redisson配置
        config.useSingleServer()
                //设置redis地址
                .setAddress("redis://" + host + ":" + port)
                //设置数据库
                .setDatabase(database);
        //创建Redisson实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
