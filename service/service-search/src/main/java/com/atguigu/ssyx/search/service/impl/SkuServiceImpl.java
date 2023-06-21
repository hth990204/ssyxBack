package com.atguigu.ssyx.search.service.impl;

import com.atguigu.ssyx.activity.client.ActivityFeignClient;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.enums.SkuType;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.search.SkuEs;
import com.atguigu.ssyx.search.repository.SkuRepository;
import com.atguigu.ssyx.search.service.SkuService;
import com.atguigu.ssyx.vo.search.SkuEsQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.ConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl implements SkuService {

    @Resource
    private ActivityFeignClient activityFeignClient;


    @Resource
    private SkuRepository skuRepository;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private RedisTemplate redisTemplate;


    @Override
    public void upperSku(Long skuId) {

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo == null) {
            return;
        }
        Category category = productFeignClient.getCategory(skuInfo.getCategoryId());

        SkuEs skuEs = new SkuEs();
        if (category != null) {
            skuEs.setCategoryId(category.getId());
            skuEs.setCategoryName(category.getName());
        }
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName()+","+skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());
        if(skuInfo.getSkuType() == SkuType.COMMON.getCode()) {
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
            skuEs.setStock(skuInfo.getStock());
            skuEs.setSale(skuInfo.getSale());
            skuEs.setPerLimit(skuInfo.getPerLimit());
        }
        skuRepository.save(skuEs);

    }

    @Override
    public void lowerSku(Long skuId) {
        skuRepository.deleteById(skuId);
    }

    @Override
    public List<SkuEs> findHotSkuList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SkuEs> pageModel = skuRepository.findByOrderByHotScoreDesc(pageable);
        List<SkuEs> skuEsList = pageModel.getContent();
        return skuEsList;
    }

    @Override
    public Page<SkuEs> search(Pageable pageable, SkuEsQueryVo skuEsQueryVo) {
        // 1.向SkuEsQueryVo 设置wareId
        Long wareId = AuthContextHolder.getWareId();
        skuEsQueryVo.setWareId(wareId);

        Page<SkuEs> pageModel = null;

        // 2.调用SkuRepository方法进行条件查询
        // 判断keyword是否为空，如果为空就根据wareId + categoryId
        String keyword = skuEsQueryVo.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            pageModel =
                    skuRepository.findByCategoryIdAndWareId(skuEsQueryVo.getCategoryId(), wareId, pageable);
        } else {
            // 不为空，wareId + categoryId + keyword
            pageModel =
                    skuRepository.findByKeywordAndWareId(keyword, wareId, pageable);
        }


        // 3.查询商品参加的营销活动
        List<SkuEs> skuEsList = pageModel.getContent();
        if (!CollectionUtils.isEmpty(skuEsList)) {
            // 得到所有skuId
            List<Long> skuIdList = skuEsList.stream().map(item -> item.getId()).collect(Collectors.toList());
            // 根据skuList远程调用，service-activity
            // 返回Map<Long, List<String>>
            // key是skuId的值
            // value是list集合，存的是股则列表

            Map<Long, List<String>> skuIdToRuleList = activityFeignClient.findActivity(skuIdList);
            // 封装获取数据到skuEs里的ruleList中
            if (!CollectionUtils.isEmpty(skuIdToRuleList)) {
                skuEsList.forEach(skuEs -> {
                    skuEs.setRuleList(skuIdToRuleList.get(skuEs.getId()));
                });
            }
        }

        return pageModel;
    }

    // 更新商品热度
    @Override
    public void incrHotScore(Long skuId) {
        String key = "hotScore";
        // redis保存数据，每次+1
        Double hotScore = redisTemplate.opsForZSet().incrementScore(key, "skuId:" + skuId, 1);
        // 规则
        if (hotScore%10 == 0) {
            //更新ES
            Optional<SkuEs> optional = skuRepository.findById(skuId);
            SkuEs skuEs = optional.get();
            skuEs.setHotScore(Math.round(hotScore));
            skuRepository.save(skuEs);
        }
    }
}
