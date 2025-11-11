package com.fz.web.controller;

import com.fz.entity.po.CategoryInfo;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.CategoryInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @Author: fz
 * @Date: 2024/12/6 00:46
 * @Description:
 */
@RestController
@RequestMapping("/category")
public class CategoryController extends ABaseController {
    @Resource
    private CategoryInfoService categoryInfoService;

    /**
     * 根据查询参数查询分类
     * @param
     * @return
     * @author fz
     * 2024/12/7 14:44
     */
    @RequestMapping("/loadAllCategory")
    public ResponseVO loadAllCategory(){
        List<CategoryInfo> categoryInfoList = categoryInfoService.getAllCategoryList();
        // 返回结果
        return getSuccessResponseVO(categoryInfoList);
    }
}
