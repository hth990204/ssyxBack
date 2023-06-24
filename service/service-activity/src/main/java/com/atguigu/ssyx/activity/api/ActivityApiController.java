package com.atguigu.ssyx.activity.api;

import com.atguigu.ssyx.activity.service.ActivityInfoService;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "促销与优惠券接口")
@RestController
@RequestMapping("/api/activity")
public class ActivityApiController {

    @Resource
    private ActivityInfoService activityInfoService;

    @ApiOperation(value = "根据skuId列表获取促销信息")
    @PostMapping("inner/findActivity")
    public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList) {
        return activityInfoService.findActivity(skuIdList);
    }

    @ApiOperation("根据skuId获取营销数据和优惠券")
    @GetMapping("/inner/findActivityAndCoupon/{skuId}/{userId}")
    public Map<String, Object> findActivityAndCoupon(@PathVariable Long skuId,
                                                     @PathVariable Long userId) {

        return activityInfoService.findActivityAndCoupon(skuId, userId);
    }

    // 获取购物车里满足条件的优惠券和活动
    @PostMapping("/inner/findCartActivityAndCoupon/{userId}")
    public OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList,
                                                    @PathVariable Long userId) {
        return activityInfoService.findCartActivityAndCoupon(cartInfoList, userId);
    };

}
