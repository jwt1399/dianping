package com.kbdp;

import com.kbdp.entity.Shop;
import com.kbdp.service.impl.ShopServiceImpl;
import com.kbdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.kbdp.utils.RedisConstants.*;

import static com.kbdp.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
class KbDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;
    private  RedissonClient redissonClient;


    @Test
    void testSaveShop() throws InterruptedException {
        shopService.saveShop2Redis(1L, 10L);
        RLock test = redissonClient.getLock("test");
        test.tryLock();
    }

    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }


    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Test
    void loadShopData() {
        // 1.??????????????????
        List<Shop> list = shopService.list();
        // 2.????????????????????????typeId?????????typeId???????????????????????????
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 3.??????????????????Redis
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            // 3.1.????????????id
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY + typeId;
            // 3.2.?????????????????????????????????
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            // 3.3.??????redis GEOADD key ?????? ?????? member
            for (Shop shop : value) {
                // stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(), shop.getY())
                ));
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }

    @Test
    void testHyperLogLog() {
        //???????????????????????????1000???????????????
        // ??????????????????????????????
        String[] users = new String[1000];
        // ????????????
        int index = 0;
        for (int i = 1; i <= 1000000; i++) {
            // ??????
            users[index++] = "user_" + i;
            // ???1000???????????????
            if (i % 1000 == 0) {
                index = 0;
                //PFADD
                stringRedisTemplate.opsForHyperLogLog().add("hll1", users);
            }
        }
        // ???????????? PFCOUNT
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hll1");
        System.out.println("size = " + size);
    }
}
