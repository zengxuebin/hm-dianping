package com.hmdp;

import com.hmdp.utils.RedisIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HmDianPingApplicationTests {

    @Autowired
    private RedisIdGenerator redisIdGenerator;

    private final ExecutorService executorService = Executors.newFixedThreadPool(500);

    @Test
    void testRedisIdGenerator() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long orderId = redisIdGenerator.generateNextId("order");
                System.out.println("orderId = " + orderId);
            }
            latch.countDown();
        };

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            executorService.submit(task);
        }

        // 等待countDown结束
        latch.await();
        long end = System.currentTimeMillis();

        System.out.println("time = " + (end - begin));

    }

    @Test
    void name() {
        System.out.println(LocalDateTime.now());
    }
}
