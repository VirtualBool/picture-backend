package com.yupi.yupicturebackend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.yupicturebackend.model.entity.Picture;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86176
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-03-18 11:52:39
* @Entity generator.domain.Picture
*/
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {

}




