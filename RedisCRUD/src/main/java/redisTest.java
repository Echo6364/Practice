import java.util.List;

import redis.clients.jedis.*;

public class redisTest implements Runnable {
    String productkey = "xiaomi";
    Jedis jedis = new Jedis("localhost");
    String userName;

    public redisTest(String userName) {
        this.userName = userName;
    }

    /**
     * 这是一个抢购系统测试
     */
    @Override
    public void run() {
        jedis.watch(productkey);
        String value = jedis.get(productkey);
        int num = Integer.valueOf(value);
        if (num <= 10000 && num >= 1) {
            Transaction tx = jedis.multi();
            tx.incrBy(productkey, -1);
            //自减
            List<Object> list = tx.exec();
            if (list == null || list.size() == 0) {
                System.out.println(userName + "商品抢购失败");
            } else {
                for (Object success : list) {
                    System.out.println(userName + "(" + success.toString() + ")商品抢购成功，当前抢购的人数是:" + (1 - (num - 10000)));
                }
            }
        } else {
            System.out.println(userName + "商品已被抢完");
        }
        jedis.close();
    }
}
