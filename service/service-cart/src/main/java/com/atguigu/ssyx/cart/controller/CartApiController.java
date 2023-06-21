package com.atguigu.ssyx.cart.controller;

import com.atguigu.ssyx.cart.service.CartInfoService;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Resource
    private CartInfoService cartInfoService;

    // 添加商品到购物车
    // userId， skuId， skuNum
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum) {
        // 获取当前用户
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.addToCart(userId, skuId, skuNum);
        return Result.ok(null);
    }

    // 根据skuId删除购物车
    @DeleteMapping("/deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId) {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteCart(skuId, userId);
        return Result.ok(null);
    }

    // 清空购物车
    @DeleteMapping("/deleteAllCart")
    public Result deleteAllCart() {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok(null);
    }

    // 批量删除购物车skuId
    @DeleteMapping("/batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList) {
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchDeleteCart(userId, skuIdList);
        return Result.ok(null);
    }

}
