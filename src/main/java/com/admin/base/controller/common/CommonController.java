package com.admin.base.controller.common;

import com.admin.base.constant.ResponseCode;
import com.admin.base.common.JsonResponse;
import com.admin.base.config.common.SysConfig;
import com.admin.base.constant.DownloadType;
import com.admin.base.exception.BusinessException;
import com.admin.base.utils.StringUtils;
import com.admin.base.utils.file.FileUploadUtils;
import com.admin.base.utils.file.FileUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/16 10:48 上午
 * @desc
 */
@Slf4j
@Controller
@RequestMapping("/common")
public class CommonController {
    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Integer type, Boolean delete, HttpServletResponse response) {
        try {
            if (FileUtils.checkAllowDownload(fileName)) {
                throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "文件名称非法，不允许下下载");
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = getFilePath(fileName, type);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    private String getFilePath(String fileName, Integer type) {
        String filePath;
        if (type == DownloadType.LOCAL) {//本地资源下载
            filePath = SysConfig.getLocalStore() + "/" + fileName;
        } else {
            filePath = SysConfig.getDownloadPath() + "/" + fileName;
        }
        return filePath;
    }

    /**
     * 通用上传请求
     */
    @PostMapping("/upload")
    @ResponseBody
    public JsonResponse uploadFile(MultipartFile file) {
        try {
            // 上传文件路径
            String filePath = SysConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);

            return JsonResponse.success(fileName);
        } catch (Exception e) {
            return JsonResponse.error(ResponseCode.CODE_SYS_ERROR, e.getMessage());
        }
    }



    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource2")
    public void resourceDownloadTest(String resource, HttpServletResponse response) {
        try {
            if (FileUtils.checkAllowDownload(resource)) {
                throw new BusinessException(ResponseCode.CODE_SYS_ERROR, "文件名称非法，不允许下下载");
            }
            // 本地资源路径
            String localPath = SysConfig.getDownloadPath() + "/" + resource;
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(localPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(localPath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }
}
