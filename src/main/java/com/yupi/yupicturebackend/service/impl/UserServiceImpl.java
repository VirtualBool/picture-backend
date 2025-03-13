package com.yupi.yupicturebackend.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.mapper.UserMapper;
import com.yupi.yupicturebackend.model.Enums.UserRoleEnum;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.LoginUserVO;
import com.yupi.yupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

import static com.yupi.yupicturebackend.constant.UserConstant.USER_LOGIN_STATE;


/**
* @author 86176
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-03-13 10:03:42
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if(StrUtil.hasBlank(userAccount, userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length() <4 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过短");
        }
        if(userPassword.length() <6){
            throw new  BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        ThrowUtils.throwIf(!userPassword .equals( checkPassword), ErrorCode.PARAMS_ERROR, "前后密码不一致");
         QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        User userB = this.baseMapper.selectOne(queryWrapper);
        if(userB != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号名重复");
        }
        String encryptPassword = getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败") );

        return user.getId();
    }

    /**
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if(StrUtil.hasBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length() <4 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过短");
        }
        if(userPassword.length() <6){
            throw new  BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", getEncryptPassword(userPassword));
        User user = this.baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(user == null, new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误"));
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if(currentUser == null || currentUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return currentUser;
    }

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 密码加密
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword){
        String SALT = "admin";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public boolean logout(HttpServletRequest request) {
        Object useroj = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(useroj == null, ErrorCode.NOT_LOGIN_ERROR);
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }
}




