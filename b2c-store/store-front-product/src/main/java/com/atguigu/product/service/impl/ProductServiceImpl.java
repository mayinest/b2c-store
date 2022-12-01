package com.atguigu.product.service.impl;

import com.atguigu.clients.CategoryClient;
import com.atguigu.clients.SearchClient;
import com.atguigu.param.ProductHotParam;
import com.atguigu.param.ProductIdsParam;
import com.atguigu.param.ProductSearchParam;
import com.atguigu.pojo.Category;
import com.atguigu.pojo.Picture;
import com.atguigu.pojo.Product;
import com.atguigu.product.mapper.PictureMapper;
import com.atguigu.product.mapper.ProductMapper;
import com.atguigu.product.service.ProductService;
import com.atguigu.to.OrderToProduct;
import com.atguigu.utils.R;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
 *  类别的实现类
 * */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    // 引入feign客户端，需要在启动类添加配置注解
    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SearchClient searchClient;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PictureMapper pictureMapper;

    /**
     * 单类别名称 查询热门商品 至多7条数据
     * 1. 根据类别名称 调用feign客户端访问类别服务
     * 2. 成功 继续根据类别id查询商品数据【热门 销售量排序 查询7】
     * 3. 结果封装即可
     *
     * @param categoryName 类别名称
     * @return r
     */
    @Cacheable(value = "list.product", key = "#categoryName", cacheManager = "cacheManagerDay")
    @Override
    public R promo(String categoryName) {

        R r = categoryClient.byName(categoryName);

        if (r.getCode().equals(R.FAIL_CODE)) {
            log.info("ProductServiceImpl.promo业务结束，结果:{}", "类别查询失败！");
            return r;
        }

        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) r.getData();

        Integer categoryId = (Integer) map.get("category_id");

        // 封装查询参数
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId);
        queryWrapper.orderByDesc("product_sales");

        IPage<Product> page = new Page<>(1, 7);
        // 返回的是包装数据！内部有对应的商品集合，也有分页的参数 例如：总条数 总页数等等
        page = productMapper.selectPage(page, queryWrapper);

        List<Product> productList = page.getRecords();
        long total = page.getTotal();

        log.info("ProductServiceImpl.promp业务结束,结果:{}", productList);
        return R.ok("数据查询成功", productList);

    }

    /**
     * 多类别热门商品查询 根据类别名称集合 至多查询7条
     * 1. 调用类别服务
     * 2. 类别集合id查询商品
     * 3. 结果集封装即可
     *
     * @param productHotParamm 类别名称集合
     * @return r
     */
    @Cacheable(value = "list.product", key = "#productHotParamm.categoryName")
    @Override
    public R hots(ProductHotParam productHotParamm) {

        R r = categoryClient.hots(productHotParamm);

        if (r.getCode().equals(R.FAIL_CODE)) {
            log.info("ProductServiceImpl.hots业务结束，结果:{}", r.getMsg());
            return r;
        }

        List<Object> ids = (List<Object>) r.getData();

        // 进行商品数据查询
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("category_id", ids);
        queryWrapper.orderByDesc("product_sales");

        IPage<Product> page = new Page<>(1, 7);

        page = productMapper.selectPage(page, queryWrapper);

        List<Product> records = page.getRecords();

        R ok = R.ok("多类别热门商品查询成功！", records);

        log.info("ProductServiceImpl.hots业务结束，结果:{}", ok);

        return ok;
    }

    /**
     * 查询类别商品集合
     *
     * @return
     */
    @Override
    public R clist() {

        R r = categoryClient.list();
        log.info("ProductServiceImpl.list业务结束,结果:{}", r);

        return r;
    }

    /**
     * 通用性的业务！
     * 传入了类别id，根绝id查询并且分页
     * 没有传入类别的id，查询全部！
     *
     * @param productIdsParam
     * @return
     */
    @Cacheable(value = "list.product", key = "#productIdsParam.categoryID+'-'+#productIdsParam.currentPage+'-'+#productIdsParam.pageSize")
    @Override
    public R byCategory(ProductIdsParam productIdsParam) {

        List<Integer> categoryID = productIdsParam.getCategoryID();

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        if (!categoryID.isEmpty()) {
            queryWrapper.in("category_id", categoryID);
        }

        IPage<Product> page = new Page<>(productIdsParam.getCurrentPage(), productIdsParam.getPageSize());

        page = productMapper.selectPage(page, queryWrapper);

        // 结果集封装
        R ok = R.ok("查询成功！", page.getRecords(), page.getTotal());

        log.info("ProductServiceImpl.byCategory业务结束，结果:{}", ok);

        return ok;
    }

    /**
     * 根据商品id，查询商品详情信息
     *
     * @param productID
     * @return
     */
    @Cacheable(value = "product", key = "#productID")
    @Override
    public R detail(Integer productID) {

        Product product = productMapper.selectById(productID);

        R ok = R.ok(product);
        log.info("ProductServiceImpl.detail业务结束,结果:{}", ok);
        return ok;
    }

    /**
     * 查询商品对应的图片详情集合
     *
     * @param productID
     * @return
     */
    @Cacheable(value = "picture", key = "#productID")
    @Override
    public R pictures(Integer productID) {

        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("product_id", productID);

        List<Picture> pictureList = pictureMapper.selectList(queryWrapper);

        R ok = R.ok(pictureList);

        log.info("ProductServiceImpl.pictures业务结束，结果:{}", ok);

        return ok;
    }

    /**
     * 搜索服务调用，获取全部商品数据
     * 进行同步
     *
     * @return 商品数据集合
     */
    @Cacheable(value = "list.category", key = "#root.methodName", cacheManager = "cacheManagerDay")
    @Override
    public List<Product> allList() {

        List<Product> productList = productMapper.selectList(null);
        log.info("ProductServiceImpl.allList业务结束，结果:{}", productList.size());
        return productList;
    }

    /**
     * 搜索业务，需要调用搜索服务！
     *
     * @param productSearchParam
     * @return
     */
    @Override
    public R search(ProductSearchParam productSearchParam) {

        R r = searchClient.search(productSearchParam);
        log.info("ProductServiceImpl.search业务结合，结果:{}", r);
        return r;
    }

    /**
     * 搜索商品id集合查询商品信息
     *
     * @param productIds
     * @return
     */
    @Cacheable(value = "list.product", key = "#productIds")
    @Override
    public R ids(List<Integer> productIds) {

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("product_id", productIds);

        List<Product> productList = productMapper.selectList(queryWrapper);

        R r = R.ok("类别信息查询成功！", productList);
        log.info("ProductServiceImpl.ids业务结束,结果:{}", r);
        return r;
    }

    /**
     * 根据商品id，查询商品id集合
     *
     * @param productIds
     * @return
     */
    @Override
    public List<Product> cartList(List<Integer> productIds) {

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("product_id", productIds);

        List<Product> productList = productMapper.selectList(queryWrapper);
        log.info("ProductServiceImpl.cartList业务结束,结果:{}", productList);
        return productList;
    }

    /**
     * 修改库存和增加销售量
     *
     * @param orderToProducts
     */
    @Override
    public void subNumber(List<OrderToProduct> orderToProducts) {

        // 将集合转成map productId orderToProduct
        Map<Integer, OrderToProduct> map = orderToProducts.stream().collect(Collectors.toMap(OrderToProduct::getProductId, v -> v));
        // 获取商品的id集合
        Set<Integer> productIds = map.keySet();
        // 查询集合对应的商品信息
        List<Product> productList = productMapper.selectBatchIds(productIds);
        // 修改商品信息
        for (Product product : productList) {
            Integer num = map.get(product.getProductId()).getNum();
            product.setProductNum(product.getProductNum() - num); // 减库存
            product.setProductSales(product.getProductSales() + num); // 加销售量
        }
        // 批量更新
        this.updateBatchById(productList);
        log.info("ProductServiceImpl.subNumber业务结束，结果:库存和销售量的修改完毕");
    }

    /**
     * 类别对应的商品数量查询
     *
     * @param categoryId
     * @return
     */
    @Override
    public Long adminCount(Integer categoryId) {

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id",categoryId);

        Long count = baseMapper.selectCount(queryWrapper);
        log.info("ProductServiceImpl.adminCount业务结束,结果:{}",count);
        return count;
    }
}
