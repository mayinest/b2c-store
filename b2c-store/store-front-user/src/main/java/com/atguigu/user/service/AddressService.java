package com.atguigu.user.service;

import com.atguigu.pojo.Address;
import com.atguigu.utils.R;

public interface AddressService {
    /**
     * 根据用户id查询 地址数据！
     * @param userId 用户id 已经校验完毕
     * @return 001 004
     */
    R list(Integer userId);

    /**
     * 插入地址数据，插入成功以后，要返回新的数据集合！
     * @param address 地址数据已经校验完毕
     * @return 数据集合
     */
    R save(Address address);

    /**
     * 根据id 删除地址数据
     * @param id 地址id
     * @return 结果001 004
     */
    R remove(Integer id);
}
