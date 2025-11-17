package com.fz.web.controller;

import java.util.HashMap;
import java.util.Map;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.annotation.RLimit;
import com.fz.component.RedisComponent;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.dto.UserCountInfoDto;
import com.fz.entity.po.UserInfo;
import com.fz.entity.vo.ResponseVO;
import com.fz.exception.BusinessException;
import com.fz.service.UserInfoService;
import com.fz.utils.IPUtils;
import com.fz.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fz.entity.constants.Constants.PASSWORD_REGEX;


/**
 * 注册登录 Controller
 */
@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;
    @Resource
    private RedisComponent redisComponent;

    /**
     * 用于用户登录/注册时将验证码数据存入redis
     *
     * @param
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
     * 发送邮箱验证码
     * @param email 邮箱
     * @return null
     * @throws MessagingException
     */
    @RequestMapping(value = "/sendEmailCode")
    @RLimit(count = 1 , time = 60)
    public ResponseVO sendEmailCode(@NotEmpty @Email @Size(max = 150) String email) throws MessagingException {
        userInfoService.sendEmailCode(email);
        return getSuccessResponseVO(null);
    }

    /**
     * 发送重置密码邮箱验证码
     * @param email 邮箱
     * @return null
     * @throws MessagingException
     */
    @RequestMapping(value = "/sendResetEmailCode")
    @RLimit(count = 1 , time = 60)
    public ResponseVO sendResetEmailCode(@NotEmpty @Email @Size(max = 150) String email) throws MessagingException {
        if (userInfoService.getUserInfoByEmail(email) == null) {
            throw new BusinessException("当前邮箱未注册，请先注册");
        }
        userInfoService.sendEmailCode(email);
        return getSuccessResponseVO(null);
    }


    @RequestMapping(value = "/sendChangeEmailCode")
    @RLimit(count = 1 , time = 60)
    @SaCheckLogin
    public ResponseVO sendChangeEmailCode() throws MessagingException {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(StpUtil.getLoginIdAsString());
        if (userInfo == null || StringTools.isEmpty(userInfo.getEmail())) {
            throw new BusinessException("账号信息异常，请重新登录");
        }
        userInfoService.sendEmailCode(userInfo.getEmail());
        return getSuccessResponseVO(null);
    }

    /**
     * 用户注册
     *
     * @param
     * @return
     * @author fz
     * 2024/12/4 23:32
     */
    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty @Email String email,
                               @NotEmpty @Size(max = 20) String nickName,
                               @NotEmpty @Pattern(regexp = PASSWORD_REGEX) String registerPassword,
                               @NotEmpty @Size(min = 6,max = 6) String emailCode,
                               @NotEmpty String checkCodeKey,
                               @NotEmpty String checkCode) {
        try {
            // 校验用户验证码
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            String cacheEmailCode = redisComponent.getEmailCode(email);
            if (StringTools.isEmpty(cacheEmailCode) || !cacheEmailCode.equals(emailCode)) {
                throw new BusinessException("邮箱验证码错误或已过期");
            }
            //  注册
            userInfoService.register(nickName, email, registerPassword);
            redisComponent.cleanEmailCode(email);
            return getSuccessResponseVO(null);
        } finally {
            // 删除图片验证码
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    /**
     * 用户重置密码
     * @param email 邮箱
     * @param newPassword 新密码
     * @param emailCode 邮箱验证码
     * @param checkCodeKey 图片验证码key
     * @param checkCode 图片验证码
     * @return
     */
    @RequestMapping("/resetPassword")
    public ResponseVO resetPassword(@NotEmpty @Email String email,
                                    @NotEmpty @Pattern(regexp = PASSWORD_REGEX) String newPassword,
                                    @NotEmpty @Size(min = 6,max = 6) String emailCode,
                                    @NotEmpty String checkCodeKey,
                                    @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            String cacheEmailCode = redisComponent.getEmailCode(email);
            if (StringTools.isEmpty(cacheEmailCode) || !cacheEmailCode.equals(emailCode)) {
                throw new BusinessException("邮箱验证码错误或已过期");
            }
            userInfoService.resetPassword(email, newPassword);
            redisComponent.cleanEmailCode(email);
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    /**
     * 用户修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return
     */
    @SaCheckLogin
    @RequestMapping("/changePassword")
    public ResponseVO changePassword(@NotEmpty String oldPassword,
                                     @NotEmpty @Pattern(regexp = PASSWORD_REGEX) String newPassword,
                                     @NotEmpty @Size(min = 6,max = 6) String emailCode) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(StpUtil.getLoginIdAsString());
        if (userInfo == null) {
            throw new BusinessException("账号状态异常，请重新登录");
        }
        String cacheEmailCode = redisComponent.getEmailCode(userInfo.getEmail());
        if (StringTools.isEmpty(cacheEmailCode) || !cacheEmailCode.equals(emailCode)) {
            throw new BusinessException("邮箱验证码错误或已过期");
        }
        userInfoService.changePassword(userInfo.getUserId(), oldPassword, newPassword);
        redisComponent.cleanEmailCode(userInfo.getEmail());
        return getSuccessResponseVO(null);
    }



    /**
     * 用户邮箱密码登录
     * @param
     * @return
     * @author fz
     * 2024/12/5 19:28
     */
    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty @Email String email,
                            @NotEmpty String password,
                            @NotEmpty String checkCodeKey,
                            @NotEmpty String checkCode) {
        try {
            // 校验用户验证码
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            // 获取ip
            String ip = IPUtils.getIp();
            // 登录逻辑 并将用户信息存入redis(token值 : tokenUserInfoDto)
            TokenUserInfoDto tokenUserInfoDto = userInfoService.login(email, password, ip);
            // 将token存入cookie,有些请求前端做不到将token放在请求头中,我们只能放在cookie中,然后从cookie中拿,StpUtil已经自动帮我们做了
            // 将token送给前端,前端要把token放在请求头中
            return getSuccessResponseVO(tokenUserInfoDto);
        } finally {
            // 删除redis中图片验证码
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    /**
     * 用户退出登录
     * @return null
     * @author fz
     * 2024/12/5 19:34
     */
    @RequestMapping("/logout")
    public ResponseVO logout() {
        // Sa-Token 会自动:
        // 1. 清除浏览器 Cookie (设置 maxAge=0)
        // 2. 删除 Redis 中的 token 数据
        // 3. 清除 Session 数据
        StpUtil.logout();
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 获取用户的硬币 关注 粉丝数
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 22:10
     */
    @RequestMapping("/getUserCountInfo")
    public ResponseVO getUserCountInfo() {
        UserCountInfoDto userCountInfo = userInfoService.getUserCountInfo(StpUtil.getLoginIdAsString());
        return getSuccessResponseVO(userCountInfo);
    }
}