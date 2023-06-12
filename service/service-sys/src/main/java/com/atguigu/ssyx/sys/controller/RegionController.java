package com.atguigu.ssyx.sys.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.sys.Region;
import com.atguigu.ssyx.sys.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 地区表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-09
 */
@Api(tags = "区域管理")
@RestController
@RequestMapping("/admin/sys/region")
public class RegionController {

    @Resource
    private RegionService regionService;

    //      url: `${api_name}/findRegionByKeyword/${keyword}`,
    //      method: 'get'
    @GetMapping("/findRegionByKeyword/{keyword}")
    @ApiOperation("根据关键字查询区域")
    public Result findRegionByKeyword(@PathVariable String keyword) {
        List<Region> list = regionService.getRegionByKeyword(keyword);
        return Result.ok(list);
    }
}

