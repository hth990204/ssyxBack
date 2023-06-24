package com.atguigu.ssyx.activity.service.impl;

import com.atguigu.ssyx.activity.mapper.CouponInfoMapper;
import com.atguigu.ssyx.activity.mapper.CouponRangeMapper;
import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.enums.CouponRangeType;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.activity.CouponRange;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券信息 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-12
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Resource
    private CouponRangeMapper couponRangeMapper;

    @Resource
    private ProductFeignClient productFeignClient;

    @Override
    public IPage<CouponInfo> selectPageCouponInfo(Long page, Long limit) {
        //	@TableField(exist = false)
        //	private String couponTypeString;
        //	@TableField(exist = false)
        //	private String rangeTypeString;
        Page<CouponInfo> pageParam = new Page<>(page, limit);
        IPage<CouponInfo> couponInfoPage = baseMapper.selectPage(pageParam, null);
        List<CouponInfo> couponInfoList = couponInfoPage.getRecords();
        couponInfoList.stream().forEach(item -> {
            item.setCouponTypeString(item.getCouponType().getComment());
            CouponRangeType rangeType = item.getRangeType();
            if (rangeType != null) {
                item.setRangeTypeString(rangeType.getComment());
            }
        });
        return couponInfoPage;
    }

    @Override
    public CouponInfo getCouponInfo(Long id) {
        CouponInfo couponInfo = baseMapper.selectById(id);
        couponInfo.setCouponTypeString(couponInfo.getCouponType().getComment());
        if (couponInfo.getRangeType() != null) {
            couponInfo.setRangeTypeString(couponInfo.getRangeType().getComment());
        }
        return couponInfo;
    }

    @Override
    public Map<String, Object> findCouponRuleList(Long id) {
        // 根据优惠券id得到优惠券信息 coupon_info
        CouponInfo couponInfo = baseMapper.selectById(id);
        // 根据优惠券id查coupon_range 查里面对象的range_id
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(
                new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, id)
        );
        List<Long> rangeIdList = couponRangeList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        //// 规则类型：SKU       range_id 就是SkuId
        //// 规则类型：CATEGORY  range_id就是分类Id
        // 分别判断封装
        //// 查skuId，远程调用得到对应的skuInfo

        //// 规则是分类，远程调用得到对应的categoryInfo

        if (!CollectionUtils.isEmpty(rangeIdList)) {
            if (couponInfo.getRangeType() == CouponRangeType.SKU) {
                List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(rangeIdList);
                result.put("skuInfoList", skuInfoList);
            } else if (couponInfo.getRangeType() == CouponRangeType.CATEGORY) {
                List<Category> categoryList = productFeignClient.findCategoryList(rangeIdList);
                result.put("categoryList", categoryList);
            }
        }
        return result;
    }

    @Override
    public void saveCouponRule(CouponRuleVo couponRuleVo) {

        couponRangeMapper.delete(
                new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId, couponRuleVo.getCouponId())
        );

        // 更新基本信息
        CouponInfo couponInfo = baseMapper.selectById(couponRuleVo.getCouponId());
        couponInfo.setRangeType(couponRuleVo.getRangeType());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setAmount(couponRuleVo.getAmount());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());
        baseMapper.updateById(couponInfo);

        // 新规则
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            couponRange.setCouponId(couponRuleVo.getCouponId());
            couponRangeMapper.insert(couponRange);
        }

    }

    // 根据skuId + userId查询优惠券信息
    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId, Long userId) {
        // 远程调用：根据skuId获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 根据条件查询：skuId + categoryId + userId
        List<CouponInfo> couponInfoList = baseMapper.selectCouponInfoList(skuId, skuInfo.getCategoryId(), userId);
        return couponInfoList;
    }

    //3 获取购物车可用优惠券列表
    @Override
    public List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId) {
        // 1 获取用户所有的优惠券
        // coupon_user   coupon_info
        List<CouponInfo> userAllCouponInfoList =
                baseMapper.selectCartCouponInfoList(userId);
        if (CollectionUtils.isEmpty(userAllCouponInfoList)) {
            return new ArrayList<CouponInfo>();
        }

        // 2 从第一步返回的list集合中，获取所有优惠券id列表
        List<Long> couponIdList = userAllCouponInfoList.stream().map(couponInfo -> couponInfo.getId())
                .collect(Collectors.toList());

        // 3 查询优惠券使用范围 coupon_range
        //couponRangeList
        LambdaQueryWrapper<CouponRange> wrapper = new LambdaQueryWrapper<>();
        // id in (1, 2, 3)
        wrapper.in(CouponRange::getCouponId, couponIdList);
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(wrapper);

        // 4 获取优惠券id 对应skuId列表
        // 优惠券 id 进行分组
        // Map<Long, List<Long>>
        Map<Long, List<Long>> couponIdToSkuIdMap =
                this.findCouponIdToSkuIdMap(cartInfoList, couponRangeList);

        // 5 遍历全部优惠券集合，判断优惠券类型
        // 全场通用  sku和分类
        BigDecimal reduceAmount = new BigDecimal(0);
        CouponInfo optimalCouponInfo = null;
        for (CouponInfo couponInfo : userAllCouponInfoList) {
            // 全场通用
            if (CouponRangeType.ALL == couponInfo.getRangeType()) {
                BigDecimal totalAmount = computeTotalAmount(cartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            } else {
                // 优惠券id获取对应的skuId列表
                List<Long> skuIdList
                        = couponIdToSkuIdMap.get(couponInfo.getId());
                // 满足适用范围购物项
                List<CartInfo> currentCartInfoList = cartInfoList.stream()
                        .filter(cartInfo -> skuIdList.contains(cartInfo.getSkuId()))
                        .collect(Collectors.toList());
                BigDecimal totalAmount = computeTotalAmount(currentCartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }
            if (couponInfo.getIsSelect().intValue() == 1 && couponInfo.getAmount().subtract(reduceAmount).doubleValue() > 0) {
                reduceAmount = couponInfo.getAmount();
                optimalCouponInfo = couponInfo;
            }
        }

        // 6 返回List<CouponInfo>
        if(null != optimalCouponInfo) {
            optimalCouponInfo.setIsOptimal(1);
        }
        return userAllCouponInfoList;

    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal(0);
        for (CartInfo cartInfo : cartInfoList) {
            BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            total = total.add(itemTotal);
        }
        return total;
    }

    // 4 获取优惠券id 对应skuId列表
    // 优惠券 id 进行分组
    private Map<Long, List<Long>> findCouponIdToSkuIdMap(List<CartInfo> cartInfoList,
                                                         List<CouponRange> couponRangeList) {
        Map<Long, List<Long>> couponIdToSkuIdMap = new HashMap<>();

        // couponRangeList 根据优惠券id分组
        Map<Long, List<CouponRange>> couponRangeToRangeListMap = couponRangeList.stream().
                collect(Collectors.groupingBy(couponRange -> couponRange.getCouponId()));

        // 遍历map
        Iterator<Map.Entry<Long, List<CouponRange>>> iterator =
                couponRangeToRangeListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, List<CouponRange>> entry = iterator.next();
            Long couponId = entry.getKey();
            List<CouponRange> rangeList = entry.getValue();

            // 创建集合 set
            Set<Long> skuIdSet = new HashSet<>();
            for (CartInfo cartInfo : cartInfoList) {
                for (CouponRange couponRange : rangeList) {
                    // 判断
                    if (couponRange.getRangeType() == CouponRangeType.SKU
                    && couponRange.getRangeId().longValue() == cartInfo.getSkuId().longValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    } else if (couponRange.getRangeType() == CouponRangeType.CATEGORY
                    && couponRange.getRangeId().longValue() == cartInfo.getCategoryId().longValue()) {
                        skuIdSet.add(cartInfo.getSkuId());
                    }
                }
            }
            couponIdToSkuIdMap.put(couponId, new ArrayList<>(skuIdSet));

        }
        return couponIdToSkuIdMap;
    }
}
