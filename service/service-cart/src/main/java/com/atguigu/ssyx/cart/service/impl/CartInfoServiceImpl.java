package com.atguigu.ssyx.cart.service.impl;

import com.atguigu.ssyx.cart.service.CartInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.client.search.SkuFeignClient;
import com.atguigu.ssyx.common.constant.RedisConst;
import com.atguigu.ssyx.common.execption.SsyxException;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.enums.SkuType;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ProductFeignClient productFeignClient;

    // 返回购物车在redis的key
    private String getCartKey(Long userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    // 添加商品到购物车
    @Override
    public void addToCart(Long userId, Long skuId, Integer skuNum) {
        //1 从redis中根据key获取数据
        // 包含userId
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> hashOperations
                = redisTemplate.boundHashOps(cartKey);

        CartInfo cartInfo = null;

        //2 根据结果，进行判断是否存在skuId
        if (hashOperations.hasKey(skuId.toString())) {
            //3 如果包含skuId，不是第一次，更新数量
            cartInfo = hashOperations.get(skuId.toString());
            // 修改数量
            int currentSkuNum = cartInfo.getSkuNum() + skuNum;
            if (currentSkuNum < 1) {
                return;
            }
            cartInfo.setSkuNum(currentSkuNum);
            cartInfo.setCurrentBuyNum(currentSkuNum);

            // 判断商品数量不能大于限购
            Integer perLimit = cartInfo.getPerLimit();
            if (currentSkuNum > perLimit) {
                throw new SsyxException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            // 更新cartInfo
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        } else {
            //4 如果没有，第一次添加
            skuNum = 1;


            // 远程调用根据skuId获取skuInfo
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            if (skuInfo == null) {
                throw new SsyxException(ResultCodeEnum.DATA_ERROR);
            }

            // 封装cartInfo
            cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }

        //5 更新redis
        hashOperations.put(skuId.toString(), cartInfo);

        //6 设置有效时间
        this.setCartKeyExpire(cartKey);
    }

    // 根据skuId删除购物车
    @Override
    public void deleteCart(Long skuId, Long userId) {
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(this.getCartKey(userId));
        if (hashOperations.hasKey(skuId.toString())) {
            hashOperations.delete(skuId.toString());
        }
    }

    // 清空购物车
    @Override
    public void deleteAllCart(Long userId) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = hashOperations.values();
        for (CartInfo cartInfo : cartInfoList) {
            hashOperations.delete(cartInfo.getSkuId().toString());
        }
    }

    // 批量删除购物车skuId
    @Override
    public void batchDeleteCart(Long userId, List<Long> skuIdList) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });
    }

    // 设置key 过期时间
    private void setCartKeyExpire(String key) {
        redisTemplate.expire(key, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }
}
