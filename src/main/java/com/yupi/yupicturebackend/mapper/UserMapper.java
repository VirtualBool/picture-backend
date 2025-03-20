package com.yupi.yupicturebackend.mapper;

import com.yupi.yupicturebackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 李鱼皮
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-12-09 20:03:03
* @Entity com.yupi.yupicturebackend.model.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




