package com.yupi.yupicturebackend.exception;

public class ThrowUtils {

    /**
     * 条件成立，抛出异常
     * @param condition 条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException){
        if(condition){
            throw runtimeException;
        }
    }

    public static void throwIf(boolean condition, ErrorCode e){
        throwIf(condition, new BusinessException(e));
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String message ){

        throwIf(condition, new BusinessException(errorCode, message));
    }




}
