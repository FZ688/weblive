package com.fz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fz.entity.po.SysRole;

import java.util.List;

/**
 * @Auther: fz
 * @Date: 2025/11/16-2:27
 * @Description: com.fz.service
 */
public interface SysRoleService extends IService<SysRole> {
    List<String> selectRolesCodeByUserId(Object loginId);
}
