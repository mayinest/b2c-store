package com.atguigu.search.service;

import com.atguigu.param.ProductSearchParam;
import com.atguigu.utils.R;

public interface SearchService {
    /**
     * 根据关键字和分页进行数据库数据查询
     * @param productSearchParam
     * @return
     */
    R search(ProductSearchParam productSearchParam);
}
