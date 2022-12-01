package com.atguigu.admin.service;

import com.atguigu.param.ProductSearchParam;
import com.atguigu.utils.R;

public interface ProductService {

    /**
     * 全部商品查询和搜索查询的方法
     * @param productSearchParam
     * @return
     */
    R search(ProductSearchParam productSearchParam);
}
