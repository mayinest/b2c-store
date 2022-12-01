package com.atguigu.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/*
*  接收前端参数的param
* TODO: 要使用jsr 303的注解 进行参数校验！
* */
@Data
public class UserCheckParam {

    /*
    *  @NotBlank 字符串不能为null和空字符串""
    *  @NotNull 字符串不能为null
    *  @NotEmpty 集合类型 集合长度不能为0
    * */
    @NotBlank
    private String userName; // 注意：参数名称要等于前端传递JSON key的名称！
}
