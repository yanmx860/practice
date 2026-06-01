package com.practice.report.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** * XXL-Job 配置 * @author ymx * @since 2026-03-08 */
@Configuration
public class XxlJobConfig {

    @Autowired
    private Environment environment;

    // XxlJobSpringExecutor：XXL-Job 执行器客户端 Bean，通过 start/destroy 管理生命周期
    // initMethod="start"：Spring 容器初始化后自动启动执行器，注册到调度中心
    // destroyMethod="destroy"：容器销毁时自动注销执行器
    // 配置属性从 application.yml 读取：
    //   xxl.job.admin.addresses —— 调度中心地址
    //   xxl.job.executor.appname —— 执行器 AppName（与调度中心注册的应用名一致）
    //   xxl.job.executor.port —— 执行器端口（默认 9999），用于调度中心回调
    //   xxl.job.executor.logpath —— 任务日志本地存储路径
    //   xxl.job.executor.logretentiondays —— 日志保留天数（默认 30）
    @Bean(initMethod = "start", destroyMethod = "destroy")
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(environment.getProperty("xxl.job.admin.addresses"));
        executor.setAppname(environment.getProperty("xxl.job.executor.appname"));
        executor.setPort(Integer.parseInt(environment.getProperty("xxl.job.executor.port", "9999")));
        executor.setLogPath(environment.getProperty("xxl.job.executor.logpath"));
        executor.setLogRetentionDays(Integer.parseInt(environment.getProperty("xxl.job.executor.logretentiondays", "30")));
        return executor;
    }
}
