package com.miaoshaproject.error;

//包装器业务异常实现
// BusinessException 和 EnumBusinessError 都实现了 CommonError 接口
public class BusinessException extends Exception implements CommonError {
    // 强关联？
    private CommonError commonError;

    // 直接接受 EnumBusinessError  的传参用于构造业务异常
    public BusinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    // 接受自定义errMsg的方式构造业务异常
    public BusinessException(CommonError commonError, String errMsg) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }


    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
