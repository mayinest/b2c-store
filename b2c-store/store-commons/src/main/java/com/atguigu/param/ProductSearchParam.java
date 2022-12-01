package com.atguigu.param;

import lombok.Data;

/**
 * 搜索关键字和分页参数集合
 */
@Data
public class ProductSearchParam extends PageParam{

    private String search;
}
