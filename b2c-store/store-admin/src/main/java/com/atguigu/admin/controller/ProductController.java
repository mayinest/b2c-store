package com.atguigu.admin.controller;

import com.atguigu.admin.service.ProductService;
import com.atguigu.param.ProductSearchParam;
import com.atguigu.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品后台管理controller
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("list")
    public R adminList(ProductSearchParam productSearchParam){

        return productService.search(productSearchParam);
    }

}
