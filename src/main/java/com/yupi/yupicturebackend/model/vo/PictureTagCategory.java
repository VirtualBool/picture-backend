package com.yupi.yupicturebackend.model.vo;

import cn.hutool.db.handler.StringHandler;
import lombok.Data;


import java.io.Serializable;
import java.util.List;

/**
 * 图片标签分类视图
 */
@Data
public class PictureTagCategory implements Serializable {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     *  分类列表
     */
    private List<String> categoryList;

}
