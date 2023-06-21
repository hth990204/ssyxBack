package com.atguigu.ssyx.cart.service;

public interface CartInfoService {
    void addToCart(Long userId, Long skuId, Integer skuNum);
}
