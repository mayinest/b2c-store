package com.atguigu.order.Service.impl;

import com.atguigu.clients.ProductClient;
import com.atguigu.order.Service.OrderService;
import com.atguigu.order.mapper.OrderMapper;
import com.atguigu.param.OrderParam;
import com.atguigu.param.ProductCollectParam;
import com.atguigu.pojo.Order;
import com.atguigu.pojo.Product;
import com.atguigu.to.OrderToProduct;
import com.atguigu.utils.R;
import com.atguigu.vo.CartVo;
import com.atguigu.vo.OrderVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ProductClient productClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 进行订单数据保存业务
     * 1.将购物车数据转成订单数据
     * 2.进行订单数据的批量插入
     * 3.商品库存修改信息
     * 4.发送购物车库存修改信息
     *
     * @param orderParam
     * @return
     */
    @Transactional
    @Override
    public R save(OrderParam orderParam) {

        // 直接调用service提供的方法
        // baseMapper内置对象 调用mapper原有的方法

        // 准备数据
        List<Integer> cartIds = new ArrayList<>();
        List<OrderToProduct> orderToProducts = new ArrayList<>();
        List<Order> orderList = new ArrayList<>();

        // 生成数据
        Integer userId = orderParam.getUserId();
        long orderId = System.currentTimeMillis();

        for (CartVo cartVo : orderParam.getProducts()) {
            cartIds.add(cartVo.getId()); // 保存删除的购物车项的id
            OrderToProduct orderToProduct = new OrderToProduct();
            orderToProduct.setNum(cartVo.getNum());
            orderToProduct.setProductId(cartVo.getProductID());
            orderToProducts.add(orderToProduct); // 商品服务修改的数据

            Order order = new Order();
            order.setOrderId(orderId);
            order.setOrderTime(orderId);
            order.setUserId(userId);
            order.setProductId(cartVo.getProductID());
            order.setProductNum(cartVo.getNum());
            order.setProductPrice(cartVo.getPrice());
            orderList.add(order);
        }

        // 订单数据批量保存
        saveBatch(orderList);

        // 发送购物车消息
        rabbitTemplate.convertAndSend("topic.ex", "clear.cart", cartIds);
        // 发送商品服务信息
        rabbitTemplate.convertAndSend("topic.ex", "sub.number", orderToProducts);
        return R.ok("订单保存成功！");
    }

    /**
     * 分组查询订单数据
     * 1.查询用户对应的全部订单项
     * 2.利用stream进行订单分组，orderId
     * 3.查询订单的全部商品集合，并stream组成map
     * 4.封装返回的OrderVo对象
     *
     * @param userId
     * @return
     */
    @Override
    public R list(Integer userId) {

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Order> list = list(queryWrapper);

        // 分组
        Map<Long, List<Order>> orderMap = list.stream().collect(Collectors.groupingBy(Order::getOrderId));

        // 查询商品数据
        List<Integer> productIds = list.stream().map(Order::getProductId).collect(Collectors.toList());

        ProductCollectParam productCollectParam = new ProductCollectParam();
        productCollectParam.setProductIds(productIds);
        List<Product> productList = productClient.cartList(productCollectParam);

        Map<Integer, Product> productMap = productList.stream().collect(Collectors.toMap(Product::getProductId, v -> v));

        //结果封装
        List<List<OrderVo>> result = new ArrayList<>();

        //遍历订单项集合
        for (List<Order> orders : orderMap.values()) {
            //封装每一个订单
            List<OrderVo> orderVos = new ArrayList<>();
            for (Order order : orders) {
                OrderVo orderVo = new OrderVo();
                BeanUtils.copyProperties(order,orderVo);
                Product product = productMap.get(order.getProductId());
                orderVo.setProductName(product.getProductName());
                orderVo.setProductPicture(product.getProductPicture());
                orderVos.add(orderVo);
            }

            result.add(orderVos);
        }
        R r = R.ok("订单数据获取成功！", result);
        log.info("OrderServiceImpl.list业务结束,结果:{}",r);
        return r;
    }
}
