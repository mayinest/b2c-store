package com.atguigu.order.Service;

import com.atguigu.param.OrderParam;
import com.atguigu.pojo.Order;
import com.atguigu.utils.R;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderService extends IService<Order> {

    /**
     *  进行订单数据保存业务
     * @param orderParam
     * @return
     */
    R save(OrderParam orderParam);

    /**
     * 分组查询订单数据
     * @param userId
     * @return
     */
    R list(Integer userId);
}
