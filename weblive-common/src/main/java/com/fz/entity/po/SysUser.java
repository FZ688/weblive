package com.fz.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fz.entity.enums.EnableStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Auther: fz
 * @Date: 2025/1/16-1:45
 * @Description: 用户表
 */
@Data
@TableName(value = "sys_user")
public class SysUser implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 登陆账号
     */
    @TableField(value = "login_no")
    private String loginNo;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 上次密码修改时间
     */
    @TableField(value = "change_time")
    private Date changeTime;

    /**
     * 登陆失败次数
     */
    @TableField(value = "login_fail_count")
    private String loginFailCount;

    /**
     * 用户状态
     */
    @TableField(value = "status")
    private EnableStatus status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 入库时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
