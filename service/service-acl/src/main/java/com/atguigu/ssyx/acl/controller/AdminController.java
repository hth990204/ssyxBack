package com.atguigu.ssyx.acl.controller;

import com.atguigu.ssyx.acl.service.AdminRoleService;
import com.atguigu.ssyx.acl.service.AdminService;
import com.atguigu.ssyx.acl.service.RoleService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.utils.MD5;
import com.atguigu.ssyx.model.acl.Admin;
import com.atguigu.ssyx.model.acl.AdminRole;
import com.atguigu.ssyx.vo.acl.AdminQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "用户接口")
@RestController
@RequestMapping("/admin/acl/user")
@CrossOrigin
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private AdminRoleService adminRoleService;

    @Resource
    private RoleService roleService;

    @GetMapping("{current}/{limit}")
    @ApiOperation("用户分页查询")
    public Result pageList(@PathVariable Long current,
                           @PathVariable Long limit,
                           AdminQueryVo adminQueryVo) {
        Page<Admin> pageParam = new Page<>(current, limit);
        IPage<Admin> pageModel = adminService.selectAdminPage(pageParam, adminQueryVo);
        return Result.ok(pageModel);
    }

    @GetMapping("/get/{id}")
    @ApiOperation("用户id查询")
    public Result get(@PathVariable Long id) {
        Admin admin = adminService.getById(id);
        return Result.ok(admin);
    }

    @PostMapping("/save")
    @ApiOperation("增加用户")
    public Result save(@RequestBody Admin admin) {
        String password = admin.getPassword();

        String passwordMD5 = MD5.encrypt(password);

        admin.setPassword(passwordMD5);

        boolean is_success = adminService.save(admin);
        if (is_success) {
            return Result.ok(null);
        }
        else {
            return Result.fail(null);
        }
    }

    @PutMapping("/update")
    @ApiOperation("修改用户")
    public Result update(@RequestBody Admin admin) {

        adminService.updateById(admin);
        return Result.ok(null);
    }

    @DeleteMapping("/remove/{id}")
    @ApiOperation("删除用户")
    public Result delete(@PathVariable Long id) {
        adminService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        adminService.removeByIds(idList);
        return Result.ok(null);
    }

    @ApiOperation("查看用户角色")
    @GetMapping("/toAssign/{id}")
    public Result getRoles(@PathVariable Long id) {
        Map<String, Object> map = roleService.getRolesByAdminId(id);
        return Result.ok(map);
    }
}
