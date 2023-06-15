package com.atguigu.ssyx.home.service.impl;

import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.client.search.SkuFeignClient;
import com.atguigu.ssyx.client.user.UserFeignClient;
import com.atguigu.ssyx.home.service.HomeService;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.search.SkuEs;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private SkuFeignClient skuFeignClient;

    // 首页数据显示
    @Override
    public Map<String, Object> homeData(Long userid) {
        Map<String, Object> result = new HashMap<>();
        // 1.根据userId获取提货地址信息
        // 远程调用service-user模块
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userid);
        result.put("leaderAddressVo", leaderAddressVo);

        // 2.获取所有分类
        // 远程调用service-product模块
        List<Category> categoryList = productFeignClient.findAllCategoryList();
        result.put("categoryList", categoryList);

        // 3.获取新人专享
        // 远程调用service-product模块
        List<SkuInfo> newPersonSkuInfoList = productFeignClient.findNewPersonSkuInfoList();
        result.put("newPersonSkuInfoList", newPersonSkuInfoList);

        // 4.获取爆款产品
        // 远程调用service-search模块
        // score 评分降序
        List<SkuEs> hotSkuList = skuFeignClient.findHotSkuList();
        result.put("hotSkuList", hotSkuList);

        // 5.封装获取的数据
        return result;
    }
}
