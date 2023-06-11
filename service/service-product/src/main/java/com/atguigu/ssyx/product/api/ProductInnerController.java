package com.atguigu.ssyx.product.api;


import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.service.CategoryService;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/product")
public class ProductInnerController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private SkuInfoService skuInfoService;

    @GetMapping("/inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable Long categoryId) {
        Category category = categoryService.getById(categoryId);
        return category;
    }

    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId) {
        SkuInfoVo skuInfo = skuInfoService.getSkuInfo(skuId);
        return skuInfo;
    }

}
