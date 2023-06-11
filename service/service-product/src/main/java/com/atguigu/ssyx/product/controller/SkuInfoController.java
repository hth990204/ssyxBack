package com.atguigu.ssyx.product.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.SkuAttrValue;
import com.atguigu.ssyx.model.product.SkuImage;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.product.SkuPoster;
import com.atguigu.ssyx.product.service.SkuAttrValueService;
import com.atguigu.ssyx.product.service.SkuImageService;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.product.service.SkuPosterService;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    //       url: `${api_name}/save`,
    //      method: 'post',
    //      data: role
    @ApiOperation("增加SKU")
    @PostMapping("/save")
    public Result save(@RequestBody SkuInfoVo skuInfoVo) {
        skuInfoService.saveSkuInfo(skuInfoVo);
        return Result.ok(null);
    }

    //      url: `${api_name}/get/${id}`,
    //      method: 'get'
    @ApiOperation("根据Id获取SKU")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id) {
        SkuInfoVo skuInfoVo = skuInfoService.getSkuInfo(id);
        return Result.ok(skuInfoVo);
    }

    //       url: `${api_name}/update`,
    //      method: 'put',
    //      data: role
    @ApiOperation("修改SKU")
    @PutMapping("/update")
    public Result update(@RequestBody SkuInfoVo skuInfoVo) {
        skuInfoService.updateSkuInfo(skuInfoVo);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        skuInfoService.removeByIdAndEls(id);
        return Result.ok(null);
    }

    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        skuInfoService.removeByIds(idList);
        return Result.ok(null);
    }

//    url: `${api_name}/check/${id}/${status}`,
//    method: 'get'
    @ApiOperation("商品审核")
    @GetMapping("/check/{id}/{status}")
    public Result check(@PathVariable Long id,
                        @PathVariable Integer status) {
        skuInfoService.check(id, status);
        return Result.ok(null);
    }

    //      url: `${api_name}/publish/${id}/${status}`,
    //      method: 'get'
    @ApiOperation("商品上下架")
    @GetMapping("/publish/{id}/{status}")
    public Result publish(@PathVariable Long id,
                          @PathVariable Integer status) {
        skuInfoService.publish(id, status);
        return Result.ok(null);
    }

    //    return request({
    //      url: `${api_name}/isNewPerson/${id}/${status}`,
    //      method: 'get'
    //    })
    //新人专享
    @GetMapping("isNewPerson/{skuId}/{status}")
    public Result isNewPerson(@PathVariable("skuId") Long skuId,
                              @PathVariable("status") Integer status) {
        skuInfoService.isNewPerson(skuId, status);
        return Result.ok(null);
    }

}

