package com.atguigu.param;

import lombok.Data;

/**
 * 分页的属性
 */
@Data
public class PageParam {

    private int currentPage = 1; // 默认值1
    private int pageSize = 15; // 默认值15
}
