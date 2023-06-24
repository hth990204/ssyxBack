package com.atguigu.ssyx.activity.service.impl;

import com.atguigu.ssyx.activity.mapper.ActivityInfoMapper;
import com.atguigu.ssyx.activity.mapper.ActivityRuleMapper;
import com.atguigu.ssyx.activity.mapper.ActivitySkuMapper;
import com.atguigu.ssyx.activity.service.ActivityInfoService;
import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.enums.ActivityType;
import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.activity.ActivityRule;
import com.atguigu.ssyx.model.activity.ActivitySku;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.ActivityRuleVo;
import com.atguigu.ssyx.vo.order.CartInfoVo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
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
 * 活动表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-12
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    @Resource
    private ActivityRuleMapper activityRuleMapper;

    @Resource
    private ActivitySkuMapper activitySkuMapper;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private CouponInfoService couponInfoService;

    @Override
    public IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam) {
        IPage<ActivityInfo> activityInfoPage = baseMapper.selectPage(pageParam, null);

        List<ActivityInfo> activityInfoList = activityInfoPage.getRecords();
        activityInfoList.stream().forEach(item -> {
            item.setActivityTypeString(item.getActivityType().getComment());
        });

        return activityInfoPage;
    }

    @Override
    public Map<String, Object> findActivityRuleList(Long id) {
        Map<String, Object> map = new HashMap<>();

        // 根据活动id查规则列表 activity_rule
        LambdaQueryWrapper<ActivityRule> activityRuleWrapper = new LambdaQueryWrapper<>();
        activityRuleWrapper.eq(ActivityRule::getActivityId, id);
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(activityRuleWrapper);
        map.put("activityRuleList", activityRuleList);


        // 根据活动id查使用规则的商品skuId activity_sku
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(
                new LambdaQueryWrapper<ActivitySku>().eq(ActivitySku::getActivityId, id));
        List<Long> skuIdList = activitySkuList.stream().map(ActivitySku::getSkuId).collect(Collectors.toList());

        List<SkuInfo> skuInfoList = new ArrayList<>();
        // 从skuId列表查商品 远程调用service-product

        if (CollectionUtils.isEmpty(skuIdList)) {
            skuInfoList = productFeignClient.findSkuInfoList(skuIdList);
        }
        map.put("skuInfoList", skuInfoList);
        return map;
    }

    @Override
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        // 删除之前的规则（规则、sku）
        Long activityId = activityRuleVo.getActivityId();
        activityRuleMapper.delete(
                new LambdaQueryWrapper<ActivityRule>().eq(ActivityRule::getActivityId, activityId)
        );

        activitySkuMapper.delete(
                new LambdaQueryWrapper<ActivitySku>().eq(ActivitySku::getActivityId, activityId)
        );

        // 获取活动规则的列表数据
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        ActivityInfo activityInfo = baseMapper.selectById(activityId);
        for (ActivityRule activityRule : activityRuleList) {
            activityRule.setActivityId(activityId);
            activityRule.setActivityType(activityInfo.getActivityType());
            activityRuleMapper.insert(activityRule);
        }
        // 获取规则范围
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        for (ActivitySku activitySku : activitySkuList) {
            activitySku.setActivityId(activityId);
            activitySkuMapper.insert(activitySku);
        }
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        // 根据关键字查询sku
        // service-product
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfo::getId).collect(Collectors.toList());
        if (skuIdList.size() == 0) {
            return skuInfoList;
        }

        // 判断商品是否参加过活动，活动进行中，排除
        // activity_info和activity_sku
        List<Long> existSkuIdList = baseMapper.selectSkuIdListExist(skuIdList);

        List<SkuInfo> findSkuList = new ArrayList<>();
        for (SkuInfo skuInfo : skuInfoList) {
            if (!existSkuIdList.contains(skuInfo.getId())) {
                findSkuList.add(skuInfo);
            }
        }
        return findSkuList;
    }

    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        Map<Long, List<String>> result = new HashMap<>();
        // 得到每个skuId
        skuIdList.forEach(skuId -> {
            // 根据skuId查询对应的活动规则列表
            List<ActivityRule> activityRuleList =
                    baseMapper.findActivityRule(skuId);
            // 数据封装
            if (!CollectionUtils.isEmpty(activityRuleList)) {
                List<String> ruleList = new ArrayList<>();
                // 处理规则名称
                for (ActivityRule activityRule : activityRuleList) {
                    ruleList.add(this.getRuleDesc(activityRule));
                }
                result.put(skuId, ruleList);
            }
        });
        return result;
    }

    // 根据skuId获取营销数据和优惠券
    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        // 根据skuId获取营销sku营销活动
        List<ActivityRule> activityRuleList = this.findActivityRuleBySkuId(skuId);

        // 根据skuId + userId查询优惠券信息
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfoList(skuId, userId);

        // 封装到map集合
        Map<String, Object> map = new HashMap<>();
        map.put("activityRuleList", activityRuleList);
        map.put("couponInfoList", couponInfoList);
        return map;
    }

    @Override
    public List<ActivityRule> findActivityRuleBySkuId(Long skuId) {
        List<ActivityRule> activityRule = baseMapper.findActivityRule(skuId);
        for (ActivityRule rule : activityRule) {
            String ruleDesc = this.getRuleDesc(rule);
            rule.setRuleDesc(ruleDesc);
        }
        return activityRule;
    }

    // 获取购物车里满足条件的优惠券和活动
    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        //1 获取购物车每个购物项参与的活动，根据活动规则分组
        // 一个规则对应多个商品
        // CartInfoVo
        List<CartInfoVo> cartInfoVoList = this.findCartActivityList(cartInfoList);

        //2 计算活动优惠金额
        BigDecimal activityReduceAmount = cartInfoVoList.stream()
                .filter(cartInfoVo -> cartInfoVo.getActivityRule() != null)
                .map(cartInfoVo -> cartInfoVo.getActivityRule().getReduceAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //3 获取购物车可用优惠券列表
        List<CouponInfo> couponInfoList =
                couponInfoService.findCartCouponInfo(cartInfoList, userId);

        //4 计算商品使用优惠券的金额，一次只能使用一张
        BigDecimal couponReduceAmount = new BigDecimal(0);
        if (!CollectionUtils.isEmpty(couponInfoList)) {
            couponReduceAmount = couponInfoList.stream()
                    .filter(couponInfo -> couponInfo.getIsOptimal().intValue() == 1)
                    .map(couponInfo -> couponInfo.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        //5 计算没有参与活动的原始金额
        BigDecimal originalTotalAmount = cartInfoList.stream()
                .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                .map(cartInfo -> cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //6 参与活动使用优惠券的总金额
        BigDecimal totalAmount
                = originalTotalAmount.subtract(activityReduceAmount).subtract(couponReduceAmount);

        //7 封装需要的数据 OrderConfirmVo
        OrderConfirmVo orderTradeVo = new OrderConfirmVo();
        orderTradeVo.setCarInfoVoList(cartInfoVoList);
        orderTradeVo.setActivityReduceAmount(activityReduceAmount);
        orderTradeVo.setCouponInfoList(couponInfoList);
        orderTradeVo.setCouponReduceAmount(couponReduceAmount);
        orderTradeVo.setOriginalTotalAmount(originalTotalAmount);
        orderTradeVo.setTotalAmount(totalAmount);

        return orderTradeVo;
    }

    // 获取购物项规则
    @Override
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        // 创建返回集合
        List<CartInfoVo> cartInfoVoList = new ArrayList<>();
        // 获取所有skuId
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());
        // 根据所有skuId获取参与活动
        List<ActivitySku> activitySkuList = baseMapper.selectCartActivity(skuIdList);
        // 根据活动进行分组，每个活动里面有哪些skuId信息
        // key为活动Id， value为每组里sku列表
        Map<Long, Set<Long>> activityIdToSkuIdListMap = activitySkuList.stream().collect(
                Collectors.groupingBy(
                        ActivitySku::getActivityId,
                        Collectors.mapping(ActivitySku::getSkuId, Collectors.toSet())
                )
        );

        // 获取活动中规则数据
        // key活动Id，value活动里的所有规则
        Map<Long, List<ActivityRule>> activityIdToActivityRuleListMap
                = new HashMap<>();
        // 所有活动Id
        Set<Long> activityIdSet
                = activitySkuList.stream().map(ActivitySku::getActivityId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(activityIdSet)) {
            // activity_rule表
            LambdaQueryWrapper<ActivityRule> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ActivityRule::getConditionAmount, ActivityRule::getConditionNum);
            wrapper.in(ActivityRule::getActivityId, activityIdSet);
            List<ActivityRule> activityRuleList = activityRuleMapper.selectList(wrapper);
            // 封装到activityIdToActivityRuleListMap
            activityIdToActivityRuleListMap = activityRuleList.stream().
                    collect(Collectors.groupingBy(activityRule -> activityRule.getActivityId()));
        }

        // 有活动的购物项的skuId
        Set<Long> activitySkuIdSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(activityIdToSkuIdListMap)) {
            Iterator<Map.Entry<Long, Set<Long>>> iterator = activityIdToSkuIdListMap.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<Long, Set<Long>> entry = iterator.next();
                // 活动id
                Long activityId = entry.getKey();
                // 每个活动对应的所有skuId
                Set<Long> currentActivitySkuIdSet = entry.getValue();

                // 获取当前活动对应的购物项列表
                List<CartInfo> currentActivityCartInfoList = cartInfoList.stream()
                        .filter(cartInfo -> currentActivitySkuIdSet.contains(cartInfo.getSkuId())).collect(Collectors.toList());

                // 计算购物项总金额和总数量
                BigDecimal activityTotalAmount =
                        this.computeTotalAmount(currentActivityCartInfoList);
                int activityTotalNum = this.computeCartNum(currentActivityCartInfoList);

                // 计算活动对应规则内容
                // 根据activityId获取活动对应规则
                List<ActivityRule> currentActivityRuleList
                        = activityIdToActivityRuleListMap.get(activityId);
                ActivityType activityType = currentActivityRuleList.get(0).getActivityType();
                // 判断活动类型（满减、打折）
                ActivityRule activityRule = null;
                if (activityType == ActivityType.FULL_REDUCTION) {
                    // 满减
                    activityRule = this.computeFullReduction(activityTotalAmount, currentActivityRuleList);
                } else {
                    // 满量
                    activityRule = this.computeFullDiscount(activityTotalNum, activityTotalAmount, currentActivityRuleList);
                }

                // CartInfoVo封装
                CartInfoVo cartInfoVo = new CartInfoVo();
                cartInfoVo.setActivityRule(activityRule);
                cartInfoVo.setCartInfoList(currentActivityCartInfoList);
                cartInfoVoList.add(cartInfoVo);

                // 记录哪些购物项参与了活动
                activitySkuIdSet.addAll(currentActivitySkuIdSet);
            }
        }

        // 没有活动的购物项skuId
        // 获取哪些skuId没有参加活动
        skuIdList.removeAll(activitySkuIdSet);
        if (!CollectionUtils.isEmpty(skuIdList)) {
            // sKuId对应的购物项
            Map<Long, CartInfo> skuIdCartInfoMap = cartInfoList.stream().collect(
                    Collectors.toMap(CartInfo::getSkuId, CartInfo -> CartInfo)
            );
            for (Long skuId : skuIdList) {
                CartInfoVo cartInfoVo = new CartInfoVo();
                cartInfoVo.setActivityRule(null);
                List<CartInfo> cartInfos = new ArrayList<>();
                cartInfos.add(skuIdCartInfoMap.get(skuId));

                cartInfoVo.setCartInfoList(cartInfos);
                cartInfoVoList.add(cartInfoVo);
            }
        }


        return cartInfoVoList;
    }

    //构造规则名称的方法
    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuffer ruleDesc = new StringBuffer();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }

    /**
     * 计算满减最优规则
     * @param totalAmount
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    /**
     * 计算满量打折最优规则
     * @param totalNum
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠折扣从大到小排序了
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    private int computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }
}
