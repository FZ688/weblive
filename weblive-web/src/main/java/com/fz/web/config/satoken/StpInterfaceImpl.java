package com.fz.web.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Auther: fz
 * @Date: 2025/11/13-23:35
 * @Description: com.fz.web.config.satoken
 */
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of("-1");
    }


    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        log.info("进入getRoleList");
        return List.of("-1");
    }
}
