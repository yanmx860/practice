package com.practice.export.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.github.pagehelper.PageHelper;
import com.practice.common.annotation.RequirePermission;
import com.practice.export.mapper.ExportOrderMapper;
import com.practice.export.model.ExportOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/** * 订单导出服务 * @author ymx * @since 2026-03-02 */
@Service
@Slf4j
public class OrderExportService {

    @Autowired
    private ExportOrderMapper mapper;

    @Autowired
    @Qualifier("exportExecutor")
    private Executor exportExecutor;

    /**
     * 百万级订单数据导出
     *
     * 方案：多线程并发分页查询 + EasyExcel 流式写出
     *
     * 防 OOM：
     *   1. PageHelper 每次只查 5000 条
     *   2. Semaphore(3) 最多 3 个查询线程同时在途
     *   3. 同一时刻只有 1 个线程持有锁写 Excel（synchronized）
     *   4. EasyExcel.inMemory(false) 写临时文件，不堆内存
     *
     * 关于排序：
     *   多线程写入顺序取决于线程调度，页间可能乱序。
     *   如需严格全局排序，建议 mapper.xml 加 ORDER BY create_time，
     *   并在 Excel 里再排一次序。
     */
    @RequirePermission("order:export")
    public void exportOrders(HttpServletResponse response, String dateRange) {

        // ==================== 1. 分页计算 ====================
        int pageSize = 5000;
        long total = mapper.selectCount(null);
        int totalPages = (int) Math.ceil((double) total / pageSize);
        log.info("开始导出, 总记录数={}, 总页数={}, 每页={}", total, totalPages, pageSize);

        // ==================== 2. 设置响应头 ====================
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        try {
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("orders.xlsx", "UTF-8"));
        } catch (IOException e) {
            log.error("设置文件名失败", e);
        }

        // ==================== 3. 多线程并发查询 + 串行写出 ====================

        // EasyExcel 流式写出（inMemory=false 写临时文件，不占堆）
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), ExportOrder.class)
                .inMemory(false)
                .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("订单数据").build();

            // CountDownLatch：等待所有分页线程处理完成后再返回（主线程 await 阻塞）
            CountDownLatch latch = new CountDownLatch(totalPages);
            // Semaphore(3)：最多 3 个查询线程同时在途——限流防止 DB 连接池耗尽
            Semaphore semaphore = new Semaphore(3);
            // synchronized(writeLock)：同一时刻只有 1 个线程能写 Excel（串行写出，避免并发写文件错乱）
            Object writeLock = new Object();

            // 提交所有分页查询到线程池
            for (int i = 1; i <= totalPages; i++) {
                int pageNum = i;

                // semaphore.acquire()：获取许可，无可用许可时阻塞，实现背压限流
                semaphore.acquire();

                exportExecutor.execute(() -> {
                    try {
                        // ----- 生产者：PageHelper 每次只查 5000 条（内存可控） -----
                        PageHelper.startPage(pageNum, pageSize);
                        List<ExportOrder> records = mapper.selectList(null);

                        // ----- 消费者：synchronized 串行写出到 Excel（线程安全） -----
                        synchronized (writeLock) {
                            excelWriter.write(records, writeSheet);
                        }

                        log.debug("第{}页完成, {}条", pageNum, records.size());

                    } catch (Exception e) {
                        log.error("第{}页导出失败", pageNum, e);
                    } finally {
                        semaphore.release();
                        latch.countDown();
                    }
                });
            }

            // 主线程等待所有分页处理完毕
            latch.await();
            log.info("导出完成, 共{}条记录", total);

        } catch (Exception e) {
            log.error("导出失败", e);
        }
    // OOM-safe 的原因：
    // 1. PageHelper 分页 5000 条/次，不会一次性查全部数据到内存
    // 2. Semaphore(3) 限制同时在途的查询线程数，防止并发过高堆积
    // 3. synchronized 串行写 Excel，不并发攒数据
    // 4. EasyExcel.inMemory(false) 写磁盘临时文件而非堆内存
    // 5. 每条数据写完即释放引用，GC 可及时回收
    }
}
