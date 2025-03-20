package com.yupi.yupicturebackend.manager;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 用户上传图片
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

    /**
     *  通过url上传图片
     * @param fileUrl 文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix){
        //校验图片
       // vaildPicture(multipartFile);
        vaildPicture(fileUrl);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
       //String originalFilename = multipartFile.getOriginalFilename();
        String originalFilename = FileUtil.mainName(fileUrl);
        String updateFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String updatePath = String.format("/%s/%s",uploadPathPrefix, updateFilename);
        File file = null;

        //创建临时文件
        try {
            file = File.createTempFile(updatePath, null);
            HttpUtil.downloadFile(fileUrl, file);
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

    public void vaildPicture(String fileUrl) {

        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {
            // 1. 验证 URL 格式
            new URL(fileUrl); // 验证是否是合法的 URL
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        // 3. 发送 HEAD 请求以验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


}

