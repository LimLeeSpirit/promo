package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnumBusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;


    @Override
    @Transactional
    public void changPassword(String telephone, String password) throws BusinessException {
        // 根据电话获取用户,判断该电话号码是否注册过，如果没有注册过，则抛出异常
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if (userDO == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_EXIST, "用户还未注册，请注册");
        }

        try {
            // 根据 telephone 获取 用户id。
            UserPasswordDO userPasswordDO = new UserPasswordDO();
            userPasswordDO.setEncrptPassword(password);
            userPasswordDO.setUserId(userDOMapper.selectByTelephone(telephone).getId());
            // 需要在 UserPasswordDOMapper 里面添加根据用户 id 更新密码的方法吗？
            userPasswordDOMapper.updateByUserIdSelective(userPasswordDO);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    @Override
    public UserModel getUserById(Integer id) {
        // 调用 UserDOMapper 获取到对应的用户的 dataobject
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);

        if (userDO == null) {
            return null;
        }
        // mapper 自己提供的仅仅是根据主键 去查，但是我们这里需要根据用户 id去查询其密码，所以需要改造一下 UserPasswordDOMapper
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO, userPasswordDO);
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);

        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }

    @Override
    public UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException {
        //通过用户手机获取用户信息
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if (userDO == null) {
            throw new BusinessException(EnumBusinessError.USER_LOGIN_FAIL);
        }

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO, userPasswordDO);

        //比对 用户信息内加密的密码 是否和 传输进来的密码 相匹配
        if (!com.alibaba.druid.util.StringUtils.equals(encrptPassword, userModel.getEncrptPassword()))  {
            throw new BusinessException(EnumBusinessError.USER_LOGIN_FAIL);
        }
        System.out.println("校验成功");
        return userModel;
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        // 校验参数是否合法
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        /**
         * 旧的参数校验
        if (StringUtils.isEmpty(userModel.getName())
                || (userModel.getGender() == null)
                || (userModel.getAge() == null)
                || StringUtils.isEmpty(userModel.getTelephone()) ) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR);
        }
         */

        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        //将 model -> dataobject
        UserDO userDO = convertFromModel(userModel);
        try {
            System.out.println("我要注册基本信息了");
            userDOMapper.insertSelective(userDO);
            System.out.println("基本信息注册成功");
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "该手机号已被注册");
        } catch (Exception ex) {
            System.out.println(ex);
        }

        userModel.setId(userDO.getId());

        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        try {
            userPasswordDOMapper.insertSelective(userPasswordDO);
            System.out.println("密码信息注册成功");
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return;
    }

    private UserDO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        System.out.println("convertFromModel 成功");
        return userDO;
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());

        System.out.println("convertPasswordFromModel 成功");
        return userPasswordDO;
    }
}
