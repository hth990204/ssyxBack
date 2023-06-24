package com.atguigu.ssyx.cart.controller;

import com.atguigu.ssyx.activity.client.ActivityFeignClient;
import com.atguigu.ssyx.cart.service.CartInfoService;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Resource
    private CartInfoService cartInfoService;

    @Resource
    private ActivityFeignClient activityFeignClient;

    // 1 根据skuId选中
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked) {
        // 获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.checkCart(userId, skuId, isChecked);
        return Result.ok(null);
    }
    // 2 全选
    @GetMapping("/checkAllCart/{isChecked}")
    public Result checkAllCart(@PathVariable Integer isChecked) {
        // 获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.checkAllCart(userId, isChecked);
        return Result.ok(null);
    }

    // 3 批量选中
    @PostMapping("batchCheckCart/{isChecked}")
    public Result batchCheckCart(@RequestBody List<Long> skuIdList,
                                 @PathVariable(value = "isChecked") Integer isChecked) {
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchCheckCart(skuIdList, userId, isChecked);
        return Result.ok(null);
    }

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

    // 购物车列表
    @GetMapping("/cartList")
    public Result cartList() {
        // 获取用户
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList =  cartInfoService.getCartList(userId);
        return Result.ok(cartInfoList);
    }

    // 查询带优惠卷的购物车
    @GetMapping("/activityCartList")
    public Result activityCartList() {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);

        OrderConfirmVo orderTradeVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        return Result.ok(orderTradeVo);
    }

}
