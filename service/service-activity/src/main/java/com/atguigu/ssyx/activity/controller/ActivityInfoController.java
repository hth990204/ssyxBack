package com.atguigu.ssyx.activity.controller;


import com.atguigu.ssyx.activity.service.ActivityInfoService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.ActivityRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-12
 */
@RestController
@RequestMapping("/admin/activity/activityInfo")
@CrossOrigin
public class ActivityInfoController {

    @Resource
    private ActivityInfoService activityInfoService;

    //    return request({
    //      url: `${api_name}/${page}/${limit}`,
    //      method: 'get'
    //    })

    @GetMapping("/{page}/{limit}")
    public Result getList(@PathVariable Long page,
                          @PathVariable Long limit) {
        Page<ActivityInfo> pageParam = new Page<>(page, limit);
        IPage<ActivityInfo> pageModel = activityInfoService.selectPage(pageParam);
        return Result.ok(pageModel);
    }

    //      url: `${api_name}/save`,
    //      method: 'post',
    //      data: role
    @PostMapping("/save")
    public Result save(@RequestBody ActivityInfo activityInfo) {
        activityInfoService.save(activityInfo);
        return Result.ok(null);
    }

    //      url: `${api_name}/get/${id}`,
    //      method: 'get'
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable Long id) {
        ActivityInfo activityInfo = activityInfoService.getById(id);
        activityInfo.setActivityTypeString(activityInfo.getActivityType().getComment());
        return Result.ok(activityInfo);
    }

    //      url: `${api_name}/update`,
    //      method: 'put',
    //      data: role
    @PutMapping("/update")
    public Result update(@RequestBody ActivityInfo activityInfo) {
        activityInfoService.updateById(activityInfo);
        return Result.ok(null);
    }

    //    return request({
    //      url: `${api_name}/remove/${id}`,
    //      method: 'delete'
    //    })
    @DeleteMapping("/remove/{id}")
    public Result removeById(@PathVariable Long id) {
        activityInfoService.removeById(id);
        return Result.ok(null);
    }

    //      url: `${api_name}/batchRemove`,
    //      method: 'delete',
    //      data: idList
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        activityInfoService.removeByIds(idList);
        return Result.ok(null);
    }

    //      url: `${api_name}/findActivityRuleList/${id}`,
    //      method: 'get'
    // 查找活动规则
    @GetMapping ("/findActivityRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id) {
        Map<String, Object> activityRuleMap = activityInfoService.findActivityRuleList(id);
        return Result.ok(activityRuleMap);
    }

    //      url: `${api_name}/saveActivityRule`,
    //      method: 'post',
    //      data: rule
    // 添加活动规则
    @PostMapping("/saveActivityRule")
    public Result saveActivityRule(@RequestBody ActivityRuleVo activityRuleVo) {
        activityInfoService.saveActivityRule(activityRuleVo);
        return Result.ok(null);
    }

    //       url: `${api_name}/findSkuInfoByKeyword/${keyword}`,
    //      method: 'get'
    // 匹配Sku信息
    @GetMapping("/findSkuInfoByKeyword/{keyword}")
    public Result findSkuInfoByKeyword(@PathVariable("keyword") String keyword) {
        List<SkuInfo> list = activityInfoService.findSkuInfoByKeyword(keyword);
        return Result.ok(list);
    }
}

