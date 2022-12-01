package com.atguigu.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/*
*  商品id参数接收
* */
@Data
public class ProductIdParam {

    @NotNull
    private Integer productID;
}
