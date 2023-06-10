package com.atguigu.ssyx.product.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * sku信息 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-10
 */
@RestController
@CrossOrigin
@RequestMapping("/admin/product/skuInfo")
public class SkuInfoController {

    @Resource
    private SkuInfoService skuInfoService;

    //      url: `${api_name}/${page}/${limit}`,
    //      method: 'get',
    //      params: searchObj
    @ApiOperation("SKU列表")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       SkuInfoQueryVo skuInfoVo) {
        Page<SkuInfo> pageParam = new Page<>(page, limit);
        IPage<SkuInfo> list = skuInfoService.selectPageSkuInfo(pageParam, skuInfoVo);
        return Result.ok(list);
    }

}

