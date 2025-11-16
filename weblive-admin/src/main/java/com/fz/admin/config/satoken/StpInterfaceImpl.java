package com.fz.admin.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.fz.entity.po.SysRole;
import com.fz.mappers.SysRoleMapper;
import com.fz.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: fz
 * @Date: 2025/11/16-2:25
 * @Description: com.fz.admin.config.satoken
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {


    private final SysRoleService sysRoleService;


    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询权限
        List<String> list = new ArrayList<String>();
        list.add("101");
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return sysRoleService.selectRolesCodeByUserId(loginId);
    }
}
