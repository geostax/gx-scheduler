package com.geostax.scheduler.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.geostax.scheduler.core.executor.ScheduleTaskExecutor;


@Configuration
@ComponentScan(basePackages = "com.geostax.scheduler.demo")
public class XxlJobConfig {
    private Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);


    @Value("${xxl.job.admin.addresses}")
    private String addresses;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.executor.logpath}")
    private String logpath;


    @Bean(initMethod = "start", destroyMethod = "destroy")
    public ScheduleTaskExecutor xxlJobExecutor() {
        logger.error("------------ xxlJobExecutor -----------");
        ScheduleTaskExecutor xxlJobExecutor = new ScheduleTaskExecutor();
        xxlJobExecutor.setIp(ip);
        xxlJobExecutor.setPort(port);
        xxlJobExecutor.setAppName(appname);
        xxlJobExecutor.setAdminAddresses(addresses);
        xxlJobExecutor.setLogPath(logpath);
        return xxlJobExecutor;
    }

}