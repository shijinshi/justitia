package cn.shijinshi.fabricmanager.service.utils.file;

import cn.shijinshi.fabricmanager.service.utils.file.exception.FileServerException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipFileUtils extends FileUtils {
    /**
     * 创建ZIP文件
     *
     * @param sourcePath 源文件或文件夹路径
     * @param zipPath    生成的zip文件保存路径（包括文件名）
     */
    public String createZip(String sourcePath, String zipPath) throws IOException {
        if (!zipPath.endsWith(".zip")) {
            throw  new RuntimeException(zipPath + "is not a zip file.");
        }

        FileOutputStream fos = new FileOutputStream(zipPath);
        ZipOutputStream zos = new ZipOutputStream(fos, Charset.forName("UTF-8"));
        File file = new File(sourcePath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    writeZip(f, "", zos);
                }
            }
        } else {
            writeZip(file, "", zos);
        }

        zos.close();
        return zipPath;
    }


    /**
     * 将整个目录内容写入zip文件输出流
     * 递归方法，当文件都写入zip输出流，或者遇到异常抛出时退出递归
     * @param file 被写入的文件或目录
     * @param parentPath zip文件内上一级目录地址，递归过程需要传递的参数
     * @param zos zip文件输出流，指向一个zip文件
     * @throws IOException
     */
    private void writeZip(File file, String parentPath, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {//处理文件夹
            parentPath += file.getName() + File.separator;
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                for (File f : files) {
                    writeZip(f, parentPath, zos);
                }
            } else {      //空目录则创建当前目录
                zos.putNextEntry(new ZipEntry(parentPath));
                zos.closeEntry();
            }
        } else {
            FileInputStream fis = new FileInputStream(file);
            ZipEntry ze = new ZipEntry(parentPath + file.getName());
            zos.putNextEntry(ze);
            byte[] content = new byte[1024];
            int len;
            while ((len = fis.read(content)) != -1) {
                zos.write(content, 0, len);
                zos.flush();
            }

            fis.close();
        }
    }


    //--------------------------------------------解压文件------------------------------------------------------
    /**
     * 解压zip文件
     * @param path 解压后文件存放路径
     * @param file 待解压的zip文件
     * @return 加压后文件路径
     * @throws FileServerException
     */
    public String unZip(String path, File file) throws FileServerException, IOException {
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(!"zip".equals(suffix)) {
            throw new FileServerException("The suffix of the file is not zip.");
        }

        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
        File dest = new File(path);
        doDecompress(dest, zis);
        zis.close();

        return path + File.separator + fileName;
    }

    /**
     * 解压zip文件输入流，写入到destFile的位置
     * @param destFile 加压后输出路径
     * @param zis 待解压的zip文件输出流
     * @throws IOException
     */
    private void doDecompress(File destFile, ZipInputStream zis) throws IOException {
        ZipEntry zipEntry = null;
        while ((zipEntry = zis.getNextEntry()) != null) {
            String dir = destFile.getPath() + File.separator + zipEntry.getName();
            File dirFile = new File(dir);
            // 如果父文件夹不存在，则递归创建其父文件夹
            fileProber(dirFile);
            if (zipEntry.isDirectory()) {
                // 如果zipEntry是目录，则创建目录
                dirFile.mkdirs();
            } else {
                // 解压压缩文件的其中具体的一个zipEntry对象
                doDecompressFile(dirFile, zis);
            }
            zis.closeEntry();
        }
    }

    /**
     * 一般意义上的文件复制操作,读取zip输入流的内容写入到文件输出流
     *
     * @param destFile 输出流，指向解压后文件输出位置
     * @param zis zip文件输入流
     * @throws IOException
     */
    private void doDecompressFile(File destFile, ZipInputStream zis) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
        int len;
        byte[] buff = new byte[1024];
        while ((len = zis.read(buff, 0, buff.length)) != -1) {
            bos.write(buff, 0, len);
        }
        bos.close();
    }
}
