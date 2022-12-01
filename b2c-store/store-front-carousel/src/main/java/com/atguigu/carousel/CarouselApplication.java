package com.atguigu.carousel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/*
*  轮播图的启动类
* */
@MapperScan(basePackages = "com.atguigu.carousel.mapper") //要扫描数据库
@SpringBootApplication
@EnableCaching
public class CarouselApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarouselApplication.class,args);
    }
}
