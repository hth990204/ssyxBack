package com.atguigu.ssyx.activity.client;

import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(value = "service-activity")
public interface ActivityFeignClient {

    @PostMapping("/api/activity/inner/findActivity")
    public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList);

    //"根据skuId获取营销数据和优惠券"
    @GetMapping("/api/activity/inner/findActivityAndCoupon/{skuId}/{userId}")
    public Map<String, Object> findActivityAndCoupon(@PathVariable("skuId") Long skuId,
                                                     @PathVariable("userId") Long userId);

    // 获取购物车里满足条件的优惠券和活动
    @PostMapping("/api/activity/inner/findCartActivityAndCoupon/{userId}")
    public OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList,
                                                    @PathVariable Long userId);
}
