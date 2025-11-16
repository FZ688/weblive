package com.fz.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fz.entity.enums.EnableStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Auther: fz
 * @Date: 2025/1/16-1:55
 * @Description: 角色表
 */
@Data
@TableName(value = "sys_role")
public class SysRole implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色编号
     */
    @TableField(value = "code")
    private String code;

    /**
     * 角色名
     */
    @TableField(value = "name")
    private String name;


    /**
     * 状态,true启用，false禁用
     */
    @TableField(value = "status")
    private EnableStatus status;

    @Serial
    private static final long serialVersionUID = 1L;
}
