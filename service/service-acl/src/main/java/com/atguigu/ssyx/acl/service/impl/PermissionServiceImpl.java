package com.atguigu.ssyx.acl.service.impl;

import com.atguigu.ssyx.acl.mapper.PermissionMapper;
import com.atguigu.ssyx.acl.service.PermissionService;
import com.atguigu.ssyx.acl.service.RolePermissionService;
import com.atguigu.ssyx.acl.service.RoleService;
import com.atguigu.ssyx.acl.utils.PermissionHandler;
import com.atguigu.ssyx.model.acl.Permission;
import com.atguigu.ssyx.model.acl.Role;
import com.atguigu.ssyx.model.acl.RolePermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Resource
    private RolePermissionService rolePermissionService;

    @Resource
    private RoleService roleService;

    @Override
    public List<Permission> queryAllPermission() {

        List<Permission> allPermissionList = baseMapper.selectList(null);

        List<Permission> result = PermissionHandler.buildPermission(allPermissionList);

        return result;
    }

    @Override
    public void removePermission(Long id) {

        List<Long> idList = new ArrayList<>();

        this.getAllChildPermission(id, idList);

        idList.add(id);

        baseMapper.deleteBatchIds(idList);
    }

    @Override
    public List<Permission> getPermissionByRoleId(Long id) {

        // 所有权限
        List<Permission> allPermissionList = baseMapper.selectList(null);

        List<Permission> res = PermissionHandler.buildPermission(allPermissionList);

        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, id);
        List<RolePermission> rolePermissionList = rolePermissionService.list(wrapper);

        // 所有已分配权限Id
        List<Long> permissionIdsList = rolePermissionList.stream()
                .map(item -> item.getPermissionId())
                .collect(Collectors.toList());

        // 已分配权限
        List<Permission> assignPermission = new ArrayList<>();

        for (Permission permission : allPermissionList) {
            if (permissionIdsList.contains(permission.getId())) {
                assignPermission.add(permission);
            }
        }

        Map<String, Object> result = new HashMap<>();

//        result.put("assignPermission", assignPermission);
//        result.put("allPermissions", allPermissionList);
//        result.put("allPermissions", res);
        return res;
    }

    @Override
    public void saveRolePermission(Long roleId, Long[] permissionIds) {
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionService.remove(wrapper);

        List<RolePermission> list = new ArrayList<>();
        for (Long permission : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setPermissionId(permission);
            rolePermission.setRoleId(roleId);
            list.add(rolePermission);
        }
        rolePermissionService.saveBatch(list);
    }

    private void getAllChildPermission(Long id, List<Long> idList) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid, id);
        List<Permission> childList = baseMapper.selectList(wrapper);
        childList.stream().forEach(item -> {
            idList.add(item.getId());
            this.getAllChildPermission(item.getId(), idList);
        });
    }
}
