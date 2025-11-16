package com.fz.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.fz.component.RedisComponent;
import com.fz.entity.dto.SysSettingDto;
import com.fz.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * @Author: fz
 * @Date: 2025/1/8 13:55
 * @Description:
 */
@RestController
@RequestMapping("/setting")
@Validated
@Slf4j
public class SettingController extends ABaseController{
    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/getSetting")
    public ResponseVO getSetting(){
       return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }


    @SaCheckRole("admin")
    @RequestMapping("/saveSetting")
    public ResponseVO saveSetting(SysSettingDto sysSettingDto){
        redisComponent.saveSettingDto(sysSettingDto);
        return getSuccessResponseVO(null);
    }
}
