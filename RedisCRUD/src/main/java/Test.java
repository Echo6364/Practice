import redis.clients.jedis.Jedis;

public class Test {
    public static void main (String [] args){
        new RedisConnectionPool();
        RedisOperation redisOperation = new RedisOperation();
        redisOperation.set("inspur", "inspurset");
        //写入
        redisOperation.delete("inspur");
        //删除
        redisOperation.set("inspur","inspur");
        redisOperation.update("inspur","inspur_update");
        //修改
        System.out.println(redisOperation.get("inspur"));
        //查询
    }

}
