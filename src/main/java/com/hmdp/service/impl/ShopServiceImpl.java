package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        /* 缓存穿透 */
        /* this::getById 指定参数和返回值类型 */
//        Shop shop = cacheClient
//                .queryWithPassThrough(RedisConstants.CACHE_SHOP_KEY, id, Shop.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        /* 互斥锁解决缓存击穿 */
        // Shop shop = queryWithMutex(id);

        /* 逻辑过期解决缓存击穿 */
        Shop shop = cacheClient
                .queryWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY, id, Shop.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        return Result.ok(shop);
    }

    /* 线程池 */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /* 逻辑过期解决缓存击穿 */
//    public Shop queryWithLogicalExpire(Long id) {
//        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
//
//        // 2.判断是否存在
//        if (StrUtil.isBlank(shopJson)) {
//            // 3.不存在，直接返回
//            return null;
//        }
//
//        // 4.存在，先把JSON反序列化为对象
//        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//        JSONObject data = (JSONObject) redisData.getData();
//        Shop shop = JSONUtil.toBean(data, Shop.class);
//        LocalDateTime expireTime = redisData.getExpireTime();
//
//        // 5.判断是否逻辑过期
//        if (expireTime.isAfter(LocalDateTime.now())) {
//            // 5.1.没过期，直接返回
//            return shop;
//        }
//
//        // 5.2.已过期，需要缓存重建
//        // 6.缓存重建
//        // 6.1.获取互斥锁
//        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
//        Boolean isLock = tryLock(lockKey);
//
//        // 6.2 如果获取成功，开启独立线程，实现缓存重建
//        if (isLock) {
//            CACHE_REBUILD_EXECUTOR.submit(() -> {
//                try {
//                    /* 查数据库，存入redis */
//                    this.saveShop2Redis(id, 20L);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    /* 释放锁 */
//                    unlock(lockKey);
//                }
//            });
//        }
//
//        // 6.3.返回过期的商户信息
//        return shop;
//    }

    /* 缓存穿透 */
//     public Shop queryWithPassThrough(Long id) {
//        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
//
//        // 2.判断是否存在
//        if (!StrUtil.isBlank(shopJson)) {
//            // 3. 存在，直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        /* 判断命中的是否是空值, null说明缓存是空的，""说明缓存里已经存了这个不存在的请求数据*/
//        if (shopJson != null) {
//            // 返回错误信息
//            return null;
//        }
//
//        // 4.redis中不存在，查mysql
//        Shop shop = getById(id);
//
//        // 5.不存在，返回错误
//        if (shop == null) {
//            // 将空值写入redis,有效期2分钟
//            stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//            return null; }
//
//        // 6.存在，写入redis
//        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//
//        // 7.返回数据
//        return shop;
//    }


    /* 互斥锁解决缓存击穿 */
//    public Shop queryWithMutex(Long id) {
//        // 1.从redis查询商户缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
//
//        // 2.判断是否存在
//        if (!StrUtil.isBlank(shopJson)) {
//            // 3. 存在，直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        /* 判断命中的是否是空值, null说明缓存是空的，""说明缓存里已经存了这个不存在的请求数据*/
//        if (shopJson != null) {
//            // 返回错误信息
//            return null;
//        }
//
//        /* 4.实现缓存重建 */
//        /* 4.1.获取互斥锁 */
//        String lockKey = "lock:" + id;
//        Shop shop = null;
//        try {
//            Boolean isLock = tryLock(lockKey);
//
//            /* 4.2.判断是否获取成功 */
//            if (!isLock) {
//                /* 4.3.如果失败，休眠后重试 */
//                Thread.sleep(50);
//
//                return queryWithMutex(id);
//            }
//
//            /* 4.4.如果成功，根据id查数据库 */
//            shop = getById(id);
//            /* 模拟重建延时 */
//            Thread.sleep(200);
//
//            // 5.不存在，返回错误
//            if (shop == null) {
//                // 将空值写入redis,有效期2分钟
//                stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null; }
//
//            // 6.存在，写入redis
//            stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            // 7.释放互斥锁
//            unlock(lockKey);
//        }
//
//        // 8.返回数据
//        return shop;
//    }

    /* 获取锁 */
//    private boolean tryLock(String key) {
//        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
//        return BooleanUtil.isTrue(flag);
//    }
//
//    /* 释放锁 */
//    private void unlock(String key) {
//        stringRedisTemplate.delete(key);
//    }
//
//    /* 封装店铺信息和逻辑过期时间到RedisData类*/
//    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
//        // 1.查询店铺数据
//        Shop shop = getById(id);
//        Thread.sleep(200);
//
//        // 2.封装成逻辑过期
//        RedisData redisData = new RedisData();
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//
//        // 3.写入redis
//        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
//    }

    @Override
    @Transactional
    public Result update(Shop shop) {

        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id为空");
        }

        // 1.更新数据库
        updateById(shop);

        // 2.删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);

        return Result.ok();
    }
}
