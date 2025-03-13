package com.yupi.yupicturebackend.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    BusinessException(int code , String message){
        super(message);
        this.code = code;
    }

    BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
         this.code = errorCode.getCode();
    }

    BusinessException(ErrorCode errorCode, String message){
        super(message);
        this.code = errorCode.getCode();
    }

}
