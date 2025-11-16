package com.fz.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fz.entity.po.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: fz
 * @Date: 2025/11/16-2:28
 * @Description: com.fz.mappers
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    List<String> selectRolesCodeByUserId(@Param("userId") Object loginId);
}
