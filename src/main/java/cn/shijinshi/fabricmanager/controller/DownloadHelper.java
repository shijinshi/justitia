package cn.shijinshi.fabricmanager.controller;

import cn.shijinshi.fabricmanager.exception.DownloadFileException;
import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.ZipFileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DownloadHelper {
    private static final Logger LOGGER = Logger.getLogger(DownloadHelper.class);


    /**
     * 创建文件下载需要的ResponseEntity
     *
     * @param file 被下载的文件或文件夹
     */
    public static ResponseEntity<byte[]> getResponseEntity(File file) throws DownloadFileException {
        if (file == null || !file.exists()) {
            throw new DownloadFileException("文件下载失败,文件或路径不存在：" + file.getPath());
        }

        try {
            if (file.isFile()) {
                return getFile(file);
            } else {
                return getDir(file);
            }
        } catch (IOException e) {
            LOGGER.warn(e);
            throw new DownloadFileException("文件下载失败：" + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param file 被下载的文件路径
     */
    private static ResponseEntity<byte[]> getFile(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        byte[] fileBytes = IOUtils.toByteArray(is);
        is.close();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;fileName=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);

    }

    /**
     * 下载整个文件目录
     *
     * @param file 被下载的文件夹路径
     */
    private static ResponseEntity<byte[]> getDir(File file) throws IOException {
        //打包成zip
        ZipFileUtils zipFileUtils = new ZipFileUtils();
        String zipPath = ExternalResources.getTemp(file.getName() + ".zip");
        try {
            zipPath = zipFileUtils.createZip(file.getPath(), zipPath);
            return getFile(new File(zipPath));
        } finally {
            FileUtils.delete(zipPath);
        }
    }
}
