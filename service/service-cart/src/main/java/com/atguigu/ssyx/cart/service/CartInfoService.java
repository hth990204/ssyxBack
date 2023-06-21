package com.atguigu.ssyx.cart.service;

import java.util.List;

public interface CartInfoService {
    void addToCart(Long userId, Long skuId, Integer skuNum);

    void deleteCart(Long skuId, Long userId);

    void deleteAllCart(Long userId);

    void batchDeleteCart(Long userId, List<Long> skuIdList);
}
