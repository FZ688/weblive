package com.fz.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: fz
 * @Date: 2025/1/16-1:58
 * @Description: 角色用户关联表
 */
@Data
@TableName(value = "role_user")
public class RoleUser implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    private Long roleId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    private static final long serialVersionUID = 1L;
}
