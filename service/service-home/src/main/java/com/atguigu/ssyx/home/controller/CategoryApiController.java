package com.atguigu.ssyx.home.controller;

import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.Category;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "商品分类")
@RestController
@RequestMapping("api/home")
public class CategoryApiController {

    @Resource
    private ProductFeignClient productFeignClient;

    // 查询所有分类
    @GetMapping("/category")
    public Result categoryList() {
        List<Category> allCategoryList = productFeignClient.findAllCategoryList();
        return Result.ok(allCategoryList);
    }


}
