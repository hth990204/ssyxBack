package com.atguigu.ssyx.acl.service;

import com.atguigu.ssyx.model.acl.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface PermissionService extends IService<Permission> {
    List<Permission> queryAllPermission();

    void removePermission(Long id);

    List<Permission> getPermissionByRoleId(Long id);

    void saveRolePermission(Long roleId, Long[] permissionId);
}
