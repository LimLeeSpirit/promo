package com.miaoshaproject.service.model;

import java.math.BigDecimal;

/**
 * 用户下单的交易模型
 */
public class OrderModel {
    private String id;

    private Integer userId;

    private Integer itemId;


    // promoId 若非空，则表示是以秒杀的形式下单的
    private Integer promoId;

    // 商品单价  promoId 若非空，则表示是以秒杀的形式下单的
    private BigDecimal itemPrice;

    // 购买件数
    private Integer amount;

    // 交易总价 promoId 若非空，则表示是以秒杀的形式下单的
    private BigDecimal orderPrice;

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }
}
