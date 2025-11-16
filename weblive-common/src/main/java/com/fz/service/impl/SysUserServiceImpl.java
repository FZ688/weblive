package com.fz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fz.entity.po.SysUser;
import com.fz.mappers.SysUserMapper;
import com.fz.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * @Auther: fz
 * @Date: 2025/11/16-2:06
 * @Description: com.fz.service.impl
 */
@Service
public class SysUserServiceImpl  extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
