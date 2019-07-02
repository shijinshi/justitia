package cn.shijinshi.fabricmanager.service.utils.file;

import cn.shijinshi.fabricmanager.service.utils.file.exception.FileServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MultipartFileUtils extends FileUtils {

    /**
     * 在basePath下保存上传的文件夹
     *
     * @param basePath
     * @param files
     */
    public String saveMultiFiles(String basePath, MultipartFile... files) throws IOException, FileServerException {
        if (files != null && files.length > 0) {
            if (!basePath.endsWith(File.separator)) {
                basePath = basePath + File.separator;
            }
            for (MultipartFile file : files) {
                saveMultiFile(basePath, file, null);
            }
            return basePath;
        } else {
            throw new FileServerException("Files is empty.");
        }
    }

    /**
     * 在basePath下保存上传的文件夹
     *
     * @param basePath
     * @param file
     */
    public String saveMultiFile(String basePath, MultipartFile file, String fileName) throws IOException, FileServerException {
        if (file == null) {
            throw new FileServerException("File is null.");
        }

        if (!basePath.endsWith(File.separator)) {
            basePath = basePath + File.separator;
        }
        String filePath;
        if (fileName == null || fileName.isEmpty()) {
            filePath = basePath + file.getOriginalFilename();
        } else {
            filePath = basePath + fileName;
        }
        File dest = new File(filePath);
        fileProber(dest);
        //transferto()方法，是springmvc封装的方法，用于文件上传时，把内存中文件写入磁盘
        file.transferTo(dest);

        return filePath;
    }

    /**
     * 把文件中内容去读成UTF-8编码字符串
     *
     * @param file
     * @return
     */
    public static String getFileString(MultipartFile file) throws IOException, FileServerException {
        if (file == null) throw new FileServerException("File is empty.");

        InputStream inputStream = file.getInputStream();
        StringBuilder sb = new StringBuilder();
        byte[] bytes = new byte[1024];
        int i = 0;
        while ((i = inputStream.read(bytes)) != -1) {
            sb.append(new String(bytes, 0 ,i));
        }
        return sb.toString();
    }

    public static Map readJsonFileAsMap(MultipartFile file) throws IOException, FileServerException {
        if (file == null)throw new FileServerException("File is empty.");

        InputStream inputStream = file.getInputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(inputStream, Map.class);
    }

}
