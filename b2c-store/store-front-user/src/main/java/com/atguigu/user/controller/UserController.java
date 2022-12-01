package com.atguigu.user.controller;

import com.atguigu.param.UserCheckParam;
import com.atguigu.param.UserLoginParam;
import com.atguigu.pojo.User;
import com.atguigu.user.service.UserService;
import com.atguigu.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userservice;

    /**
     * 检查帐号是否可用的接口
     * @param userCheckParam 接收检查的帐号实体，内部有参数校验注解
     * @param result 获取校验结果的实体对象
     * @return 返回封装结果R对象即可
     * @RequestBody 接收JSON
     */
    @PostMapping("check")
    public R check(@RequestBody @Validated UserCheckParam userCheckParam, BindingResult result){

        // 检查是否符合检验注解的规则 符合 false 不符合 true
        boolean b = result.hasErrors();

        if (b){
            return R.fail("帐号为null，不可使用！");
        }
            return userservice.check(userCheckParam);
    }

    @PostMapping("register")
    public R register(@RequestBody @Validated User user,BindingResult result){

        if (result.hasErrors()){
            // 如果存在异常，证明请求参数不符合注解要求
            return R.fail("参数异常，不可注册！");
        }
        return userservice.register(user);
    }

    @PostMapping("login")
    public R login(@RequestBody @Validated UserLoginParam userLoginParam,BindingResult result){

        if (result.hasErrors()){
            // 如果存在异常，证明请求参数不符合注解要求
            return R.fail("参数异常，不可登录！");
        }
        return userservice.login(userLoginParam);
    }
}
