package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EnumBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Random;


/**
 *
 */
@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    // 用户修改密码接口
    @RequestMapping(value = "/changePassword", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType changePassword(@RequestParam(name = "telephone") String telephone,
                                           @RequestParam(name = "otpCode") String otpCode,
                                           @RequestParam(name = "password") String password
                                           ) throws BusinessException {

        // 验证手机号和获取到的 otpCode 相匹配
        System.out.println("new otpCode:" + this.httpServletRequest.getSession().getAttribute(telephone));
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if (!StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不匹配");
        }

        // 如果匹配，则调用 service 服务进行修改密码
        userService.changPassword(telephone, this.EncodeByBase64(password));

        return CommonReturnType.create(null);
    }

    //用户登录接口
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telephone") String telephone,
                                  @RequestParam(name = "password") String password) throws BusinessException {

        System.out.println("登录的 入参校验");
        //入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telephone) || org.apache.commons.lang3.StringUtils.isEmpty(password)) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        // 对密码和手机号在数据库中进行查找，校验
        UserModel userModel = userService.validateLogin(telephone, this.EncodeByBase64(password));

        //将登陆凭证加入到用户登录成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonReturnType.create(null);

    }

    // 用户注册接口
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telephone") String telephone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password
                                     ) throws BusinessException {
        // 验证手机号和注册用的 otp 相匹配
        System.out.println("otpCode:" + this.httpServletRequest.getSession().getAttribute(telephone));
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if (!StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不匹配");
        }

        // 如果合法，则处理注册流程: 先基本信息
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender.byteValue());
        userModel.setTelephone(telephone);
        userModel.setRegisterMode("byPhone");
        // 再对密码加密
        userModel.setEncrptPassword(EncodeByBase64(password));
        System.out.println("注册调用前，加密密码为：" + userModel.getEncrptPassword());  // 之前的加密算法为 null,说明这个加密算法确实不行

        // 问题的根源：这个注册方法被调用了，只是在执行 sql 语句的时候出错了，重新分析 sql 语句的执行情况。
        userService.register(userModel);

        System.out.println(userModel.getAge());
        System.out.println("注册调用后，加密密码为：" + userModel.getEncrptPassword());  // 为 null,说明这个加密算法确实不行
        return CommonReturnType.create(null);
    }

    //密码加密
    private String EncodeByBase64(String str) {
        //确定计算方法
        try {
            String s = new String(Base64.encodeBase64(str.getBytes("UTF-8")) );
            return s;
        } catch (UnsupportedEncodingException ex) {
            ex.getStackTrace();
        }
        return null;
    }

    // 用户获取 otp 短信
    @RequestMapping(value = "/getOtp", method = {RequestMethod.POST}, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telephone") String telephone) {
        // 需要按照一定规则生成 otp 验证码
        Random random = new Random();
        int randomInt = random.nextInt(9999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        // 将 otp 验证码 和对应用户手机号关联, 使用 httpsession 的方式绑定手机号与OTPCDOE

        // 这里没设置成功吗
        this.httpServletRequest.getSession().setAttribute(telephone, otpCode);
        // System.out.println(this.httpServletRequest.getSession().getAttribute(telephone));
        // 将otp验证码 通过短信发送给用户（暂时不做,这里只打印出来）
        System.out.println("telephone: " + telephone + " & otpCode: " + otpCode);

        return CommonReturnType.create(null);
    }



    // 根据id获取用户信息
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException{
        // 通过调用 service 服务获取对应 id 的 userModel 并返回给前端
        UserModel userModel = userService.getUserById(id);

        // 若用户不存在
        if (userModel == null) {
            // throw new BusinessException(EnumBusinessError.USER_NOT_EXIST);
            userModel.setEncrptPassword("w2332");
        }
        // 不将 核心领域模型对象 userModel 返回给前端，再获取包含一部分属性的  userVO 返回给前端
        UserVO userVO =  convertFromUserModel(userModel);

        // 返回通用对象
        return CommonReturnType.create(userVO);

    }

    private UserVO convertFromUserModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
        }
}
