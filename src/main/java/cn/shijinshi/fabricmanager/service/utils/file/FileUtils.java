package cn.shijinshi.fabricmanager.service.utils.file;

import cn.shijinshi.fabricmanager.service.utils.file.exception.FileServerException;
import org.apache.commons.io.IOUtils;
import org.springframework.retry.annotation.Retryable;

import java.io.*;

public class FileUtils extends org.apache.commons.io.FileUtils {

    /**
     * 检查文件（或路径）的上层路径是否存在，如果不存在则创建这个上层路径
     *
     * @param dirFile 被检查的文件路径
     */
    public static void fileProber(File dirFile) {
        File parentFile = dirFile.getParentFile();
        if (!parentFile.exists()) {
            fileProber(parentFile);
            parentFile.mkdirs();
        }
    }

    /**
     * 确保目录存在，不存在则创建
     *
     * @param dirPath 文件路径
     */
    public static void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            fileProber(file);
            file.mkdirs();
        }
    }



    public static String writeStringToFile(String basePath, String str, String fileName) throws IOException {
        if (!basePath.endsWith(File.separator)) {
            basePath = basePath + File.separator;
        }
        String filePath;
        if (fileName == null || fileName.isEmpty()) {
            throw new NullPointerException("File name is null.");
        } else {
            filePath = basePath + fileName;
        }
        File dest = new File(filePath);
        fileProber(dest);

        FileWriter fileWriter = new FileWriter(filePath, false);
        fileWriter.write(str);
        fileWriter.flush();
        fileWriter.close();

        return filePath;
    }

    public static String readFileAsString(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        return new String(IOUtils.toByteArray(is));
    }


    //--------------------------------------------删除文件------------------------------------------------------

    /**
     * 删除本地文件或目录
     *
     * @param path 文件或目录路径
     */
    public static boolean delete(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path is empty.");
        }
        File file = new File(path);
        if (file.exists()) {
            if (file.isFile()) {
                return deleteFile(file);
            } else {
                return deleteDirectory(path);
            }
        }
        return true;
    }

    /**
     * 删除指定文件
     *
     * @param file 被删除的文件
     */
    @Retryable(value = FileServerException.class, maxAttempts = 2)
    private static boolean deleteFile(File file) {
        return file.delete();
    }

    /**
     * 删除文件目录，以及目录里面的全部内容
     *
     * @param dir 被删除的目录
     */
    private static boolean deleteDirectory(String dir) {
        boolean deleted = true;
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator)) dir = dir + File.separator;
        File dirFile = new File(dir);

        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (!deleteFile(file)) {
                    deleted = false;
                }
            } else if (file.isDirectory()) {
                if (!deleteDirectory(file.getAbsolutePath())) {
                    deleted = false;
                }
            }
        }
        // 删除当前目录
        if (!deleteFile(dirFile)) {
            deleted = false;
        }
        return deleted;
    }
}
