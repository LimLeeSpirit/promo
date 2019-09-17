package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDoMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDo;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnumBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDoMapper orderDoMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(@RequestParam(name = "user_id") Integer userId,
                                  @RequestParam(name = "item_id") Integer itemId,
                                  @RequestParam(name = "promo_id") Integer promoId,
                                  @RequestParam(name = "amount") Integer amount)
            throws BusinessException {

        // 1. 校验下单状态，用户身份合法？商品合法？数量正确？
        // 验证商品是否存在
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "商品不存在");
        }

        // 用户是否存在
        UserModel userModel = userService.getUserById(userId);
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "用户不存在");
        }

        // 商品数量是否合法
        if (amount < 0 || amount > 99) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "数量必须在100件以内");
        }

        // 校验秒杀活动信息
        if (promoId != null) {
            // 校验对应活动是否存在这个对应商品
            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            // 校验活动是否正在进行
            } else if (itemModel.getPromoModel().getStatus() != 2) {
                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "活动未进行");
            }
        }

        // 2. 落单减库存，或者支付减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EnumBusinessError.STOCK_NOT_ENOUGH);
        }

        OrderModel orderModel = new OrderModel();

        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());

            System.out.println(itemModel.getPromoModel().getStartDate());

        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply( new BigDecimal(amount)));
        System.out.println("创建订单基本信息成功");

        // 3.1  生成订单流水号，也就是订单号 id
        orderModel.setId(generateOrderNumber());
        System.out.println("生成订单号成功");

        OrderDo orderDo = convertFromOrderModel(orderModel);
        orderDoMapper.insertSelective(orderDo);
        System.out.println("创建订单成功了哦");

        // 增加销量
        itemService.increaseSales(itemId, amount);
        System.out.println("销量增加成功");

        // 4. 返回前端
        return orderModel;
    }

    //不管该方法是否在事务中，都会开启一个新的事务，不管外部事务是否成功
    //最终都会提交掉该事务，为了保证订单号的唯一性，防止下单失败后订单号的回滚
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNumber() {
        // 订单有16位
        StringBuilder sb = new StringBuilder();
        // 前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        sb.append(nowDate);

        //中间6位为自增序列
        //从数据库表中得到序列，获取当前sequence
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        int sequence;
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getStep() + sequenceDO.getCurrentValue());
        sequenceDOMapper.updateByPrimaryKey(sequenceDO);

        String sequenceStr = String.valueOf(sequence);
        // 凑足 6 位
        for (int i=0; i < 6 - sequenceStr.length(); i++) {
            sb.append(i);
        }
        sb.append(sequenceStr);

        //最后两位为分库分表位,暂时不考虑
        sb.append("00");

        return sb.toString();

    }

    private OrderDo convertFromOrderModel (OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDo orderDo = new OrderDo();
        BeanUtils.copyProperties(orderModel, orderDo);
        return orderDo;
    }
}
