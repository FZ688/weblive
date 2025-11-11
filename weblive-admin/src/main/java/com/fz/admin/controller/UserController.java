package com.fz.admin.controller;

import com.fz.entity.query.UserInfoQuery;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.UserInfoService;
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
@RequestMapping("/user")
@Validated
@Slf4j
public class UserController extends ABaseController{
    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/loadUser")
    public ResponseVO loadUser(UserInfoQuery userInfoQuery){
       userInfoQuery.setOrderBy("join_time desc");
       return getSuccessResponseVO(userInfoService.findListByPage(userInfoQuery));
    }

    @RequestMapping("/changeStatus")
    public ResponseVO loadUser(String userId,Integer status){
        userInfoService.changeUserStatus(userId,status);
        return getSuccessResponseVO(null);
    }

}
