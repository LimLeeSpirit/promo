package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

public interface PromoService {

    // 根据 商品id 获取即将进行或者正在进行的秒杀模型
    PromoModel getPromoModelById(Integer id);
}
