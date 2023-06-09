package com.atguigu.ssyx.acl.utils;

import com.atguigu.ssyx.acl.service.PermissionService;
import com.atguigu.ssyx.model.acl.Permission;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class PermissionHandler {

    @Resource
    PermissionService permissionService;

    public static List<Permission> buildPermission(List<Permission> allPermissionList) {
        List<Permission> result = new ArrayList<>();
        for (Permission permission : allPermissionList) {
            if (permission.getPid() == 0) {
                permission.setLevel(1);
                result.add(findChildren(permission, allPermissionList));
            }
        }
        return result;
    }

    private static Permission findChildren(Permission permission, List<Permission> allPermissionList) {

        permission.setChildren(new ArrayList<>());

        for (Permission it : allPermissionList) {
            if (it.getPid().equals(permission.getId())) {
                int level = permission.getLevel() + 1;
                it.setLevel(level);
                if (permission.getChildren() == null) {
                    permission.setChildren(new ArrayList<>());
                }
                permission.getChildren().add(findChildren(it, allPermissionList));
            }
        }

        return permission;
    }
}
