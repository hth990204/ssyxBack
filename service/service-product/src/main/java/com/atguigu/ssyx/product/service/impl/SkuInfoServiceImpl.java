package com.atguigu.ssyx.product.service.impl;

import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.mapper.SkuInfoMapper;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * sku信息 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-06-10
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {


    @Override
    public IPage<SkuInfo> selectPageSkuInfo(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo) {
        String keyword = skuInfoQueryVo.getKeyword();
        Long categoryId = skuInfoQueryVo.getCategoryId();
        Integer skuType = skuInfoQueryVo.getSkuType();
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(keyword)) {
            wrapper.like(SkuInfo::getSkuName, keyword);
        }

        if (!StringUtils.isEmpty(categoryId)) {
            wrapper.eq(SkuInfo::getCategoryId, categoryId);
        }

        if (!StringUtils.isEmpty(skuType)) {
            wrapper.eq(SkuInfo::getSkuType, skuType);
        }

        IPage<SkuInfo> skuInfoPage = baseMapper.selectPage(pageParam, wrapper);
        return skuInfoPage;
    }
}
