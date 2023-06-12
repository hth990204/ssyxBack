package com.atguigu.ssyx.activity.controller;


import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.vo.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * <p>
 * 优惠券信息 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-12
 */
@RestController
@RequestMapping("/admin/activity/couponInfo")
public class CouponInfoController {

    @Resource
    private CouponInfoService couponInfoService;

    // 分页查询
    // url: `${api_name}/${page}/${limit}`,
    //      method: 'get'
    @GetMapping("/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit) {
        IPage<CouponInfo> pageModel = couponInfoService.selectPageCouponInfo(page, limit);
        return Result.ok(pageModel);
    }

    // 添加优惠券
    //      url: `${api_name}/save`,
    //      method: 'post',
    //      data: role
    @PostMapping("/save")
    public Result save(@RequestBody CouponInfo couponInfo) {
        couponInfoService.save(couponInfo);
        return Result.ok(null);
    }

    // 根据id查询优惠券
    //      url: `${api_name}/get/${id}`,
    //      method: 'get'
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable Long id) {
        CouponInfo couponInfo = couponInfoService.getCouponInfo(id);
        return Result.ok(couponInfo);
    }

    // 根据优惠券id查询规则
    //      url: `${api_name}/findCouponRuleList/${id}`,
    //      method: 'get'
    @GetMapping("/findCouponRuleList/{id}")
    public Result findCouponRuleList(@PathVariable Long id) {
        Map<String, Object> map = couponInfoService.findCouponRuleList(id);
        return Result.ok(map);
    }

    // 5添加优惠券规则
    //    return request({
    //      url: `${api_name}/saveCouponRule`,
    //      method: 'post',
    //      data: rule
    //    })
    @PostMapping("/saveCouponRule")
    public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo) {
        couponInfoService.saveCouponRule(couponRuleVo);
        return Result.ok(null);
    }

}

