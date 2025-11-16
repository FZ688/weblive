package com.fz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fz.entity.po.SysRole;
import com.fz.mappers.SysRoleMapper;
import com.fz.service.SysRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Auther: fz
 * @Date: 2025/11/16-2:27
 * @Description: com.fz.service.impl
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Override
    public List<String> selectRolesCodeByUserId(Object loginId) {
        return sysRoleMapper.selectRolesCodeByUserId(loginId);
    }
}
