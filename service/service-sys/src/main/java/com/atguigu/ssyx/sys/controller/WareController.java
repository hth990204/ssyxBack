package com.atguigu.ssyx.sys.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.sys.Ware;
import com.atguigu.ssyx.sys.service.WareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 仓库表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-06-09
 */
@Api(tags = "仓库接口")
@RestController
@RequestMapping("/admin/sys/ware")
public class WareController {

    @Resource
    private WareService wareService;

    //  findAllList() {
    //    return request({
    //      url: `${api_name}/findAllList`,
    //      method: 'get'
    @GetMapping("/findAllList")
    @ApiOperation("查询所有仓库")
    public Result findAllList() {
        List<Ware> list = wareService.list();
        return Result.ok(list);
    }

}

