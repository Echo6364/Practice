import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 这是抢购系统的测试
 */
public class TestMs {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost");
        jedis.set("xiaomi", "500");
        jedis.close();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        //多线程
        for (int i = 0; i <= 10000; i++) {
            executor.execute(new redisTest("user" + i));
        }
        executor.shutdown();

    }

}