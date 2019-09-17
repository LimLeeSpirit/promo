package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;


public interface UserService {

    // 通过用户 ID 获取真正的用户对象
    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;


    void changPassword(String telephone, String password) throws BusinessException;

    /**
     * @param telephone 用户注册手机
     * @param encrptPassword 加密后的密码
     * @return
     * @throws BusinessException
     */
    UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException;



}
