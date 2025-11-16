package com.fz.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fz.component.RedisComponent;
import com.fz.entity.config.AppConfig;
import com.fz.entity.constants.Constants;
import com.fz.entity.enums.EnableStatus;
import com.fz.entity.po.SysUser;
import com.fz.entity.vo.ResponseVO;
import com.fz.exception.BusinessException;
import com.fz.service.SysUserService;
import com.fz.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: fz
 * @Date: 2024/12/6 00:46
 * @Description: 注册登录 Controller
 */
@RestController
@RequestMapping("/account")
@Validated
@Slf4j
public class AccountController extends ABaseController {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private RedisComponent redisComponent;

    /**
     * 用于用户登录/注册时将验证码数据存入redis
     * @return VO
     * @author fz
     * 2024/12/4 22:15
     */
    @RequestMapping("/checkCode")
    public ResponseVO checkCode() {
        // 生成一个图片对象
        ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(100, 42);
        // 获取图片表示的文本(验证码)
        String checkCode = arithmeticCaptcha.text();
        // 将验证码存入redis
        String checkCodeKey = redisComponent.saveCheckCode(checkCode);
        // 将当前用户的验证码key与图片与返回前端
        Map<String, Object> map = new HashMap<>();
        // 图片以Base64编码
        String checkCodeBase64 = arithmeticCaptcha.toBase64();
        map.put("checkCodeKey", checkCodeKey);
        map.put("checkCode", checkCodeBase64);
        return getSuccessResponseVO(map);
    }

    /**
     * 用户密码登录
     * @param account 账号
     * @param password 密码,前端已经做过md5加密
     * @param checkCodeKey 验证码key
     * @param checkCode 验证码
     * @return VO
     * @author fz
     * 2024/12/5 19:28
     */
    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String account,
                            @NotEmpty String password,
                            @NotEmpty String checkCodeKey,
                            @NotEmpty String checkCode) {
        try {
            // 校验用户验证码
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            SysUser sysUser = sysUserService.lambdaQuery().eq(SysUser::getLoginNo, account).one();
            // 去配置文件中读取管理员信息 比较是否一致
            if (sysUser == null) {
                throw new BusinessException("账号或密码错误,登录失败");
            }
            if (!sysUser.getPassword().equals(password)) {
                throw new BusinessException("账号或密码错误,登录失败");
            }
            if (EnableStatus.disable.equals(sysUser.getStatus())) {
                throw new BusinessException("用户已失效,登录失败");
            }
            StpUtil.login(sysUser.getId());
            // 将信息返回前端 实际上还应该将token返回给前端 但是这个项目里前端从cookie中取了token
            return getSuccessResponseVO(account);
        } finally {
            // 删除redis中图片验证码
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    /**
     * 用户退出登录
     *
     * @param
     * @return
     * @author fz
     * 2024/12/5 19:34
     */
    @RequestMapping("/logout")
    public ResponseVO logout() {
        // 清除redis与cookie中的cookie
        // Sa-Token 会自动:
        // 1. 清除浏览器 Cookie (设置 maxAge=0)
        // 2. 删除 Redis 中的 token 数据
        // 3. 清除 Session 数据
        StpUtil.logout();
        return getSuccessResponseVO(null);
    }
}