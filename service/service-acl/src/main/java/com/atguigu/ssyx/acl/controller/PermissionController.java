package com.atguigu.ssyx.acl.controller;

import com.atguigu.ssyx.acl.service.PermissionService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.acl.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "菜单管理")
@RestController
@RequestMapping("/admin/acl/permission")
@CrossOrigin
public class PermissionController {

    @Resource
    private PermissionService permissionService;



    @ApiOperation("查询所有菜单")
    @GetMapping
    public Result list() {
        List<Permission> list = permissionService.queryAllPermission();
        return Result.ok(list);
    }


    @ApiOperation("添加菜单")
    @PostMapping("/save")
    public Result addPermission(@RequestBody Permission permission) {
        permissionService.save(permission);
        return Result.ok(null);
    }


    @ApiOperation("修改菜单")
    @PutMapping("/update")
    public Result updatePermission(@RequestBody Permission permission) {
        permissionService.updateById(permission);
        return Result.ok(null);
    }


    @ApiOperation("删除菜单")
    @DeleteMapping("/remove/{id}")
    public Result removePermission(@PathVariable Long id) {
        permissionService.removePermission(id);
        return Result.ok(null);
    }

    @ApiOperation("查看角色权限列表")
    @GetMapping("/toAssign/{id}")
    public Result toAssign(@PathVariable Long id) {
        List<Permission> res = permissionService.getPermissionByRoleId(id);
        return Result.ok(res);
    }

    @ApiOperation("授予角色权限")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestParam Long roleId,
                           @RequestParam Long[] permissionId) {
        permissionService.saveRolePermission(roleId, permissionId);
        return Result.ok(null);
    }
}
