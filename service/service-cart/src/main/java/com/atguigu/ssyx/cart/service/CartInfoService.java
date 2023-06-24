package com.atguigu.ssyx.cart.service;

import com.atguigu.ssyx.model.order.CartInfo;

import java.util.List;

public interface CartInfoService {
    void addToCart(Long userId, Long skuId, Integer skuNum);

    void deleteCart(Long skuId, Long userId);

    void deleteAllCart(Long userId);

    void batchDeleteCart(Long userId, List<Long> skuIdList);

    List<CartInfo> getCartList(Long userId);

    //根据skuId选中
    void checkCart(Long userId, Long skuId, Integer isChecked);

    // 全选
    void checkAllCart(Long userId, Integer isChecked);

    // 批量选中
    void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked);
}
