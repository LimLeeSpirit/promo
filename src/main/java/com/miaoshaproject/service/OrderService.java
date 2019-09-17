package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

public interface OrderService {
    // 1. 通过前端url传来秒杀活动ID，然后下单接口内检验对于id是否属于对应商品且活动已开始

    // 2. 直接在下单接口内判断对应的商品是否存在秒杀活动，若存在则以秒杀价格下单

    // 这两种方法谁更好？ 1 更好，因为用2的话，需要在下单接口对所有订单进行校验。  1只需要校验一个参数
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;
}
