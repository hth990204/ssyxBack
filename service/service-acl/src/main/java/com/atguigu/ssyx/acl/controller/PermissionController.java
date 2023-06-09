package com.atguigu.ssyx.acl.controller;

import com.atguigu.ssyx.acl.service.PermissionService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.acl.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "菜单管理")
@RestController
@RequestMapping("/admin/acl/permission")
@CrossOrigin
public class PermissionController {

    @Resource
    private PermissionService permissionService;


    /*
      getPermissionList() {
    return request({
      url: `${api_name}`,
      method: 'get'
    })
  },
     */


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

    /*
      updatePermission(permission) {
    return request({
      url: `${api_name}/update`,
      method: "put",
      data: permission
    })
  },
     */
    @ApiOperation("修改菜单")
    @PutMapping("/update")
    public Result updatePermission(@RequestBody Permission permission) {
        permissionService.updateById(permission);
        return Result.ok(null);
    }

      /*
  删除一个权限项

    removePermission(id) {
        return request({
                url: `${api_name}/remove/${id}`,
        method: "delete"
    })
    },
            */
    @ApiOperation("删除菜单")
    @DeleteMapping("/remove/{id}")
    public Result removePermission(@PathVariable Long id) {
        permissionService.removePermission(id);
        return Result.ok(null);
    }

}
