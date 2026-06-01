package com.practice.order.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class XxlJobConfig {

    @Autowired
    private Environment environment;

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(environment.getProperty("xxl.job.admin.addresses"));
        executor.setAppname(environment.getProperty("xxl.job.executor.appname"));
        executor.setPort(Integer.parseInt(environment.getProperty("xxl.job.executor.port", "9998")));
        executor.setLogPath(environment.getProperty("xxl.job.executor.logpath"));
        executor.setLogRetentionDays(Integer.parseInt(environment.getProperty("xxl.job.executor.logretentiondays", "30")));
        return executor;
    }
}
