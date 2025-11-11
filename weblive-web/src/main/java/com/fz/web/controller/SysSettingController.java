package com.fz.web.controller;

import com.fz.component.RedisComponent;
import com.fz.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Author: fz
 * @Date: 2024/12/8 15:14
 * @Description:
 */
@RestController
@RequestMapping("/sysSetting")
public class SysSettingController extends ABaseController {
    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/getSetting")
    public ResponseVO getSetting(){
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }
}
