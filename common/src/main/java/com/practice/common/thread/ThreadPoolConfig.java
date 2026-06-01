package com.practice.common.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/** * 线程池配置 * @author ymx * @since 2026-01-15 */
@Configuration
public class ThreadPoolConfig {

    /*
     * 通用线程池：处理日常业务请求如异步通知、日志记录、消息推送。
     * core=10 适配常规并发量，max=20 应对突发流量，queue=200 缓冲排队任务。
     * 采用 CallerRunsPolicy：当队列和最大线程都满时，由提交任务的线程（如 Tomcat 线程）
     * 直接执行，天然形成反压，避免任务丢失。
     */
    @Bean("commonExecutor")
    public Executor commonExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("common-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /*
     * 导出线程池：专用于报表导出等 IO 密集型任务，core=5 控制资源消耗，
     * max=10 留有弹性。空闲线程 60s 后回收，避免长期占用内存。
     * 等待终止时间设为 60s 确保导出任务在应用关闭前完成。
     */
    @Bean("exportExecutor")
    public Executor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("export-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
