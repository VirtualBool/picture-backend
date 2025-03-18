package com.yupi.yupicturebackend.manager;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.yupi.yupicturebackend.config.CosClientConfig;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     * @param multipartFile 文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
   public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix){
       //校验图片
       vaildPicture(multipartFile);
       // 图片上传地址
       String uuid = RandomUtil.randomString(16);
       String originalFilename = multipartFile.getOriginalFilename();
       String updateFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
       String updatePath = String.format("/%s/%s",uploadPathPrefix, updateFilename);
       File file = null;

       //创建临时文件
       try {
           file = File.createTempFile(updatePath, null);
           multipartFile.transferTo(file);
           //上传图片
           PutObjectResult putObjectResult = cosManager.putPictureObject(updatePath, file);
           ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
           // 封装返回结果
           UploadPictureResult uploadPictureResult = new UploadPictureResult();

           int width = imageInfo.getWidth();
           int height = imageInfo.getHeight();
           double Scale = NumberUtil.round(width*1.0/height, 2).doubleValue();

           uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+ updatePath);
           uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
           uploadPictureResult.setPicSize(FileUtil.size(file));
           uploadPictureResult.setPicWidth(width);
           uploadPictureResult.setPicHeight(height);
           uploadPictureResult.setPicScale(Scale);
           uploadPictureResult.setPicFormat(imageInfo.getFormat());
           return uploadPictureResult;


       } catch (Exception e) {
           log.error(" 文件上传到对象存储错误", e);
           throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");

       }finally {
           deleteTempFile(file);
       }


   }

    public void deleteTempFile(File file) {
       if(file == null){
           return ;
       }
       // 删除临时文件
        boolean deleteResult = file.delete();
       if(!deleteResult){
           log.error("临时文件删除失败");
       }

    }

    /**
     * 校验文件
     * @param multipartFile 文件
     */
    public void vaildPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long filesize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(filesize > 2*ONE_M, ErrorCode.OPERATION_ERROR, "文件大小不能超过2M");
        // 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "png","jpeg","webp");

        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix),ErrorCode.PARAMS_ERROR, "文件类型错误" );



    }

}

