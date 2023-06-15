package com.atguigu.ssyx.search.service;

import com.atguigu.ssyx.model.search.SkuEs;

import java.util.List;

public interface SkuService {
    void upperSku(Long skuId);

    void lowerSku(Long skuId);

    List<SkuEs> findHotSkuList();
}
